# SosiProject - Health & Wellbeing Management System

SosiProject is a premium JavaFX application designed to help users manage their health and wellbeing through various specialized modules.

## 🚀 Key Features

- **Bien-être Mental**: Evaluate and track stress levels based on environmental and physical indicators.
- **Cycle Menstruel**: Dedicated tracking for women's health.
- **Aide & Don**: Platform for mutual aid and donations.
- **Services Médicaux**: Quick access and management of medical services.
- **Secure Authentication**: Robust login and registration system.

## 🏗️ Architecture

The project follows a modular **MVC (Model-View-Controller)** pattern for clean separation of concerns:

- **Frontend**: JavaFX (FXML) with Vanilla CSS for a premium, modern UI.
- **Backend Layers**:
    - **Model**: Ported from Symfony entities for data structure consistency.
    - **Service**: JDBC-based service layer for direct database interaction.
    - **Controller**: Logic handlers for UI events and data flow.
- **Database**: MySQL (Local instance), managed via JDBC.

## 🛠️ Tech Stack

- **Languge**: Java 17+
- **UI Framework**: JavaFX
- **Build Tool**: Maven
- **Database**: MySQL 8.0
- **Documentation**: Markdown

## 📦 Project Structure

```text
src/main/
├── java/org/example/
│   ├── home/           # Dashboard and Navigation logic
│   ├── wellbeing/      # Wellbeing and Stress prediction module
│   ├── user/           # Authentication and Profile management
│   ├── cycle/          # Cycle tracking module
│   ├── aideEtdon/      # Aid and Donation module
│   ├── servicesociaux/ # Medical services module
│   ├── utils/          # Database connection and Session management
│   └── IService/       # Core service interface
└── resources/
    ├── home/           # Main UI views and Dashboard styles
    ├── assets/         # Images and icons
    └── [module]/       # Module-specific FXML and CSS
```

## ⚙️ Setup Instructions

1. **Database Configuration**:
   - Ensure MySQL is running on `localhost:3306`.
   - Create a database named `firstproject`.
   - Run the SQL scripts provided in the documentation artifacts or `database_schema.md`.

2. **Application Configuration**:
   - Update `src/main/java/org/example/utils/MyConnection.java` with your MySQL credentials, as the password is currently set to empty.

3. **Running the App**:
   - Build with Maven: `mvn clean install`
   - Run the entry point: `MainFX.java`

## 🤝 Contribution

This project was built with a focus on **visual excellence** and **modular design**. Ensure all new components follow the existing premium styling guidelines.
