# 🏥 Hospital Management System

A full-stack **Hospital Management System** built with **Java, Javalin, PostgreSQL, HTML, CSS, JavaScript, Docker, GitHub Codespaces, Neon and Render**.

The system provides a web-based interface for managing patients, doctors, appointments, treatments, medicines, prescriptions and billing. All data is stored persistently in a cloud-hosted PostgreSQL database and accessed through a Java REST API.

> **From patient registration to billing — one integrated hospital management platform.**

---

## ✨ Features

### 📊 Live Dashboard

The dashboard provides real-time hospital statistics:

* 👨‍⚕️ Total doctors
* 🧑‍🤝‍🧑 Total patients
* 📅 Appointments for a selected date
* 💰 Total unpaid bills
* 💊 Low-stock medicines
* 🕐 Live clock
* 🔄 Automatic dashboard refresh

---

### 👤 Patient Management

Manage patient information including:

* Patient name
* Age
* Gender
* Phone number
* Address
* Admission date

Features include:

* Add patients
* Search patients
* Filter by date
* Delete records
* View patient information

---

### 👨‍⚕️ Doctor Management

Store and manage doctor information:

* Doctor name
* Specialization
* Phone number
* Consultation fee

---

### 📅 Appointment Management

Create and manage appointments between patients and doctors.

Each appointment contains:

* Patient
* Doctor
* Appointment date
* Appointment time
* Status

Supported appointment statuses:

`Scheduled` → `Arrived` → `Completed`

or

`Cancelled`

Appointment status can be updated directly from the table.

---

### 🩺 Treatment Management

Record treatments provided to patients.

The system stores:

* Patient
* Doctor
* Diagnosis
* Treatment
* Treatment date

SQL `JOIN`s allow the application to display actual patient and doctor names instead of only database IDs.

---

### 💊 Medicine & Prescription Management

Manage hospital medicines and stock.

Medicine records contain:

* Medicine name
* Price
* Available stock

Prescriptions contain:

* Patient
* Medicine
* Dosage
* Number of days

The dashboard also identifies medicines with low stock.

---

### 💳 Billing

Manage patient bills with:

* Patient
* Amount
* Payment status
* Billing date

Payment status can be changed directly between:

`Paid` / `Unpaid`

The dashboard also calculates the total unpaid amount.

---

## 🗄️ Database

The project uses **PostgreSQL** as its relational database.

### Database Tables

| Table           | Purpose                               |
| --------------- | ------------------------------------- |
| `patients`      | Stores patient information            |
| `doctors`       | Stores doctor information             |
| `appointments`  | Connects patients and doctors         |
| `treatments`    | Stores diagnoses and treatments       |
| `medicines`     | Stores medicine and stock information |
| `prescriptions` | Connects patients with medicines      |
| `bills`         | Stores billing information            |

### Database Relationships

The database uses:

* Primary Keys
* Foreign Keys
* Referential Integrity
* `ON DELETE` rules
* SQL JOINs
* Constraints

For example:

```text
Patient
   │
   ├── Appointments ─── Doctor
   │
   ├── Treatments ───── Doctor
   │
   ├── Prescriptions ── Medicine
   │
   └── Bills
```

The database schema uses:

```sql
CREATE TABLE IF NOT EXISTS
```

This allows the required tables to be created automatically when the application starts.

---

## ⚙️ System Architecture

```text
                  ┌─────────────────────┐
                  │      User / Browser  │
                  └──────────┬──────────┘
                             │
                             │ HTTP / HTTPS
                             ▼
                  ┌─────────────────────┐
                  │   HTML / CSS / JS   │
                  │     Frontend        │
                  └──────────┬──────────┘
                             │
                             │ REST API
                             ▼
                  ┌─────────────────────┐
                  │   Java + Javalin    │
                  │      Backend        │
                  └──────────┬──────────┘
                             │
                             │ JDBC
                             ▼
                  ┌─────────────────────┐
                  │ PostgreSQL Database │
                  │       Neon          │
                  └─────────────────────┘
```

### Deployment Architecture

```text
GitHub
   │
   ▼
Render
   │
   ├── Docker Container
   │
   └── Java/Javalin Application
             │
             │ DATABASE_URL
             ▼
        Neon PostgreSQL
```

---

# 🛠️ Tech Stack

### Backend

* **Java**
* **Javalin**
* **JDBC**
* REST API
* Maven

### Frontend

* **HTML5**
* **CSS3**
* **JavaScript**
* Responsive UI

### Database

* **PostgreSQL**
* **Neon Cloud**

### DevOps & Deployment

* **Docker**
* **GitHub**
* **GitHub Codespaces**
* **Render**
* HTTPS

---

# 🔌 REST API

The frontend communicates with the Java backend through REST APIs.

### HTTP Methods

| Method   | Purpose                |
| -------- | ---------------------- |
| `GET`    | Retrieve records       |
| `POST`   | Create records         |
| `PATCH`  | Update specific fields |
| `DELETE` | Delete records         |

Example:

```http
GET /api/patients
```

```http
POST /api/patients
```

```http
PATCH /api/appointments/{id}
```

```http
DELETE /api/patients/{id}
```

The backend also provides a dashboard endpoint that returns calculated statistics such as patient count, doctor count, appointments and unpaid bills.

---

# 🔐 Security

Security was considered while designing the backend.

### SQL Injection Protection

Database inserts and updates use Java `PreparedStatement`.

Instead of directly building SQL using user input:

```java
"SELECT * FROM patients WHERE name = '" + input + "'"
```

the application uses parameterized queries:

```java
PreparedStatement stmt =
    connection.prepareStatement(
        "SELECT * FROM patients WHERE name = ?"
    );

stmt.setString(1, input);
```

This prevents user input from being interpreted as SQL code.

### Table Whitelisting

The database viewer does not allow arbitrary table names.

Only predefined tables are accepted through a whitelist.

### Read-Only Database Viewer

The database viewer is restricted to `SELECT` operations.

It cannot be used to execute:

```sql
INSERT
UPDATE
DELETE
DROP
ALTER
```

### Environment Variables

Database credentials are not hard-coded into the source code.

The application uses:

```text
DATABASE_URL
```

as an environment variable.

---

# 🌐 Database Viewer

The project includes a separate **Database Viewer** page.

It allows users to:

* View raw database tables
* Execute `SELECT` queries
* Inspect stored data
* Verify that website operations are actually reflected in PostgreSQL

Example:

```sql
SELECT * FROM patients;
```

This provides a simple way to demonstrate the connection between the web application and the actual SQL database.

> ⚠️ The current database viewer is intended for demonstration and learning. It should not be publicly exposed in a production hospital system containing real patient information.

---

# 🐳 Docker

The application is containerized using Docker.

A Docker container packages the application and its runtime environment together.

Typical flow:

```text
Source Code
     ↓
Maven Build
     ↓
JAR File
     ↓
Docker Image
     ↓
Docker Container
     ↓
Render
```

This makes deployment more consistent across environments.

---

# 🚀 Deployment

The application is deployed using **Render**.

### Deployment Flow

```text
Developer
    │
    ▼
Git Push
    │
    ▼
GitHub Repository
    │
    ▼
Render
    │
    ├── Builds Docker Image
    │
    ├── Starts Java Application
    │
    └── Provides HTTPS
             │
             ▼
       Public Web Application
             │
             ▼
       Neon PostgreSQL
```

Every new push to the connected GitHub repository can trigger a new deployment.

---

# 💻 Development Environment

The project was developed using **GitHub Codespaces**.

This allowed the team to develop the application in a cloud-based development environment without depending completely on local machine resources.

Development workflow:

```text
GitHub Codespaces
       ↓
Java + Maven
       ↓
Javalin Backend
       ↓
PostgreSQL / Neon
       ↓
GitHub
       ↓
Render
```

---

# 📁 Project Structure

```text
hospital-management-system/
│
├── src/
│   └── main/
│       ├── java/
│       │   └── ...
│       │
│       └── resources/
│           ├── public/
│           │   ├── index.html
│           │   ├── database.html
│           │   ├── style.css
│           │   └── script.js
│           │
│           └── ...
│
├── Dockerfile
├── pom.xml
├── README.md
└── ...
```

> The exact structure may vary depending on the implementation.

---

# 👥 Team & Responsibilities

| Member       | Role               | Responsibilities                                                     |
| ------------ | ------------------ | -------------------------------------------------------------------- |
| **Member 1** | Database Engineer  | PostgreSQL schema, tables, relationships, constraints, Neon database |
| **Member 2** | Backend Developer  | Java, Javalin, REST APIs, JDBC, security                             |
| **Member 3** | Frontend Developer | HTML, CSS, JavaScript, dashboard, forms, search and database viewer  |
| **Member 4** | DevOps & Testing   | Maven, Docker, GitHub, Render, deployment and testing                |

---

# 🧪 Testing

The complete application flow was tested after deployment.

### Functional Testing

The team tested:

* Patient creation
* Doctor creation
* Appointment creation
* Treatment records
* Medicine records
* Prescriptions
* Bills
* Appointment status updates
* Bill payment status
* Searching
* Date filtering
* Record deletion

### Persistence Testing

A record was added through the live website and then verified directly in the Neon PostgreSQL database.

The server was restarted and the record was checked again.

The data remained available because the application stores data in the external PostgreSQL database rather than server memory or local files.

---

# ⚠️ Current Limitations

This project is primarily designed as a **college project, demonstration and learning application**.

Current limitations include:

* No authentication system
* No role-based access control
* Database viewer is publicly accessible in the demo version
* Free hosting may sleep after inactivity
* First request after sleeping may take longer
* Full record editing is limited
* Billing is not yet automatically calculated from services and medicines
* No production-grade audit logging

**Real patient data should not be entered into the current demo deployment.**

---

# 🔮 Future Improvements

Possible future development includes:

### 🔐 Authentication & Authorization

Implement:

* Admin login
* Doctor login
* Receptionist login
* Role-based permissions
* Session management

### 💰 Smart Billing

Automatically calculate:

```text
Doctor Consultation Fee
        +
Medicine Charges
        +
Treatment Charges
        ↓
Final Bill
```

### 📄 Documents

Generate:

* Printable bills
* Prescriptions
* Treatment summaries
* Patient reports

### 📊 Advanced Analytics

Add:

* Appointment analytics
* Revenue reports
* Medicine consumption
* Doctor statistics
* Patient trends

### 🔒 Production Security

Implement:

* Authentication
* Authorization
* HTTPS-only access
* Database access restrictions
* Audit logs
* Input validation
* Rate limiting
* Secure secrets management

---

# 🎯 Project Objectives

The main objectives of this project are to demonstrate how a real-world web application can combine:

```text
Relational Database
        +
Backend API
        +
Frontend UI
        +
Cloud Database
        +
Containerization
        +
Cloud Deployment
```

The project also demonstrates practical concepts such as:

* Database normalization
* Primary and foreign keys
* SQL JOINs
* REST APIs
* JDBC
* PreparedStatements
* Environment variables
* Docker
* Cloud deployment
* HTTPS
* Git-based development

---

# 🌟 What We Learned

Through this project, we gained practical experience in:

* Designing relational databases
* Connecting Java applications to PostgreSQL
* Building REST APIs
* Developing responsive web interfaces
* Writing secure SQL queries
* Using cloud databases
* Containerizing applications with Docker
* Deploying applications to cloud platforms
* Working collaboratively with Git and GitHub
* Testing an end-to-end production-like workflow

---



# 🚀 Running the Project Locally

## Prerequisites

Install:

* Java
* Maven
* Git
* PostgreSQL or a Neon PostgreSQL database
* Docker *(optional)*

---

## 1. Clone the Repository

```bash
git clone https://github.com/YOUR-USERNAME/YOUR-REPOSITORY.git
cd YOUR-REPOSITORY
```

---

## 2. Configure the Database

Create a PostgreSQL database and obtain its connection URL.

Set:

```text
DATABASE_URL
```

as an environment variable.

Example:

```bash
export DATABASE_URL="your-postgresql-connection-string"
```

On Windows PowerShell:

```powershell
$env:DATABASE_URL="your-postgresql-connection-string"
```

---

## 3. Build the Project

```bash
mvn clean package
```

---

## 4. Run the Application

```bash
java -jar target/*.jar
```

Then open the application in your browser.

---

# 🐳 Running with Docker

Build the Docker image:

```bash
docker build -t hospital-management-system .
```

Run the container:

```bash
docker run -p 8080:8080 \
  -e DATABASE_URL="your-postgresql-connection-string" \
  hospital-management-system
```

---

# 📌 Important Note

This application is a **student project / educational demonstration** and is not intended for handling real patient information.

A real hospital deployment would require significantly stronger security, authentication, authorization, privacy controls, auditing, compliance measures and infrastructure.

---

# 📜 License

This project is created for **educational and academic purposes**.

Add an appropriate open-source license if you plan to distribute or reuse the project publicly.

---

# 🙌 Acknowledgements

Built as a collaborative full-stack project using:

**Java • Javalin • PostgreSQL • Neon • HTML • CSS • JavaScript • Docker • GitHub Codespaces • Render**

---

## ⭐ If you found this project useful

Give the repository a ⭐ and feel free to explore the code, improve the system and build your own version.

**Built with code, collaboration and cloud technology. 🏥💻**
