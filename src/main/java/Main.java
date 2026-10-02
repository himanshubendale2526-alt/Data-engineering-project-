import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import java.net.URI;
import java.sql.*;
import java.util.*;

public class Main {
  static final Map<String, String> Q = new LinkedHashMap<>();
  static {
    Q.put("patients", "select * from patients order by id desc");
    Q.put("doctors", "select * from doctors order by id desc");
    Q.put("medicines", "select * from medicines order by id desc");
    Q.put("appointments", "select a.id, p.name patient, d.name doctor, a.appt_date, a.appt_time, a.status from appointments a left join patients p on p.id=a.patient_id left join doctors d on d.id=a.doctor_id order by a.appt_date desc, a.appt_time, a.id desc");
    Q.put("treatments", "select t.id, p.name patient, d.name doctor, t.diagnosis, t.treatment, t.treated_on from treatments t left join patients p on p.id=t.patient_id left join doctors d on d.id=t.doctor_id order by t.id desc");
    Q.put("prescriptions", "select r.id, p.name patient, m.name medicine, r.dosage, r.days from prescriptions r left join patients p on p.id=r.patient_id left join medicines m on m.id=r.medicine_id order by r.id desc");
    Q.put("bills", "select b.id, p.name patient, b.amount, b.paid, b.billed_on from bills b left join patients p on p.id=b.patient_id order by b.id desc");
  }

  static Connection conn() throws Exception {
    String url = System.getenv().getOrDefault("DATABASE_URL", "postgresql://hospital:hospital123@localhost:5432/hospital");
    URI u = new URI(url.replaceFirst("^postgres://", "postgresql://"));
    String[] ui = u.getUserInfo().split(":", 2);
    boolean local = u.getHost().equals("localhost");
    String jdbc = "jdbc:postgresql://" + u.getHost() + ":" + (u.getPort() < 0 ? 5432 : u.getPort())
        + u.getPath() + "?stringtype=unspecified" + (local ? "" : "&sslmode=require");
    return DriverManager.getConnection(jdbc, ui[0], ui[1]);
  }

  static String tbl(String t) {
    if (!Q.containsKey(t)) throw new RuntimeException("Invalid table");
    return t;
  }

  static List<Map<String, Object>> rows(ResultSet rs) throws SQLException {
    List<Map<String, Object>> out = new ArrayList<>();
    ResultSetMetaData m = rs.getMetaData();
    while (rs.next()) {
      Map<String, Object> r = new LinkedHashMap<>();
      for (int i = 1; i <= m.getColumnCount(); i++) {
        Object v = rs.getObject(i);
        if (v != null && !(v instanceof Number) && !(v instanceof Boolean)) v = v.toString();
        r.put(m.getColumnLabel(i), v);
      }
      out.add(r);
    }
    return out;
  }

  static List<Map<String, Object>> q(Connection c, String sql, String... a) throws SQLException {
    try (PreparedStatement p = c.prepareStatement(sql)) {
      for (int i = 0; i < a.length; i++) p.setString(i + 1, a[i]);
      return rows(p.executeQuery());
    }
  }

  static List<String> cols(Connection c, String t) throws SQLException {
    return q(c, "select column_name from information_schema.columns where table_schema='public' and table_name=? order by ordinal_position", t)
        .stream().map(m -> (String) m.get("column_name")).toList();
  }

  @SuppressWarnings("unchecked")
  public static void main(String[] args) throws Exception {
    try (Connection c = conn(); Statement s = c.createStatement()) {
      s.execute(new String(Main.class.getResourceAsStream("/schema.sql").readAllBytes()));
    }
    Javalin app = Javalin.create(cfg -> cfg.staticFiles.add("/public", Location.CLASSPATH));
    app.exception(Exception.class, (e, ctx) -> ctx.status(400).json(Map.of("error", String.valueOf(e.getMessage()))));

    app.get("/api/dash", ctx -> {
      String d = ctx.queryParam("date");
      try (Connection c = conn()) {
        Map<String, Object> o = new LinkedHashMap<>();
        o.put("stats", q(c, "select (select count(*) from patients) patients, (select count(*) from doctors) doctors, "
            + "(select count(*) from appointments where appt_date=?::date) today, "
            + "(select coalesce(sum(amount),0) from bills where not paid) unpaid, "
            + "(select count(*) from medicines where stock<10) low", d).get(0));
        o.put("list", q(c, "select * from (" + Q.get("appointments") + ") x where appt_date=?::date order by appt_time nulls last", d));
        o.put("recent", q(c, "select id, name, age, phone, admitted_on from patients order by id desc limit 5"));
        ctx.json(o);
      }
    });

    app.post("/api/sql", ctx -> {
      String q = String.valueOf(ctx.bodyAsClass(Map.class).get("q")).trim();
      if (!q.toLowerCase().startsWith("select")) throw new RuntimeException("Fakt SELECT challa");
      try (Connection c = conn()) {
        c.setReadOnly(true);
        try (Statement s = c.createStatement()) { ctx.json(rows(s.executeQuery(q))); }
      }
    });

    app.get("/api/{t}", ctx -> {
      String t = tbl(ctx.pathParam("t"));
      try (Connection c = conn()) { ctx.json(q(c, Q.get(t))); }
    });

    app.post("/api/{t}", ctx -> {
      String t = tbl(ctx.pathParam("t"));
      Map<String, Object> b = ctx.bodyAsClass(Map.class);
      try (Connection c = conn()) {
        List<String> valid = cols(c, t), k = new ArrayList<>(), v = new ArrayList<>();
        for (var e : b.entrySet())
          if (valid.contains(e.getKey()) && !e.getKey().equals("id") && e.getValue() != null && !e.getValue().toString().isBlank()) {
            k.add(e.getKey()); v.add(e.getValue().toString());
          }
        if (k.isEmpty()) throw new RuntimeException("Data bhara");
        String sql = "insert into " + t + "(" + String.join(",", k) + ") values(" + "?,".repeat(k.size() - 1) + "?)";
        try (PreparedStatement p = c.prepareStatement(sql)) {
          for (int i = 0; i < v.size(); i++) p.setString(i + 1, v.get(i));
          p.executeUpdate();
        }
        ctx.json(Map.of("ok", true));
      }
    });

    app.patch("/api/{t}/{id}", ctx -> {
      String t = tbl(ctx.pathParam("t"));
      Map<String, Object> b = ctx.bodyAsClass(Map.class);
      String col = String.valueOf(b.get("col"));
      try (Connection c = conn()) {
        if (col.equals("id") || !cols(c, t).contains(col)) throw new RuntimeException("Invalid column");
        try (PreparedStatement p = c.prepareStatement("update " + t + " set " + col + "=? where id=?")) {
          p.setString(1, String.valueOf(b.get("val")));
          p.setInt(2, Integer.parseInt(ctx.pathParam("id")));
          p.executeUpdate();
        }
        ctx.json(Map.of("ok", true));
      }
    });

    app.delete("/api/{t}/{id}", ctx -> {
      String t = tbl(ctx.pathParam("t"));
      try (Connection c = conn(); PreparedStatement p = c.prepareStatement("delete from " + t + " where id=?")) {
        p.setInt(1, Integer.parseInt(ctx.pathParam("id")));
        p.executeUpdate();
        ctx.json(Map.of("ok", true));
      }
    });

    app.start(Integer.parseInt(System.getenv().getOrDefault("PORT", "8080")));
  }
}
