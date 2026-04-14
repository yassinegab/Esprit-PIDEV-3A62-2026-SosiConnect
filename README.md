<p align="center">
  <img width="452" alt="Wellness Connect Logo" src="https://github.com/user-attachments/assets/d61ed0dc-07ff-477d-acfd-060616a6a601" />
</p>

# 🌿 SosI Connect - Wellbeing Ecosystem

## Overview

This project was developed as part of the PIDEV – 3rd Year Engineering Program at **Esprit School of Engineering** (Academic Year 2025–2026).

SOSI Connect is a comprehensive personal wellbeing tracking solution. This project demonstrates the integration between a high-performance desktop application and a robust web infrastructure.

## 🚀 Key Features

- **Bien-être Mental**: Evaluate and track stress levels based on environmental and physical indicators.
- **Cycle Menstruel**: Dedicated tracking for women's health.
- **Aide & Don**: Platform for mutual aid and donations.
- **Services Médicaux**: Quick access and management of medical services.
- **Secure Authentication**: Robust login and registration system.

<<<<<<< HEAD
---

## 🧘 Focus: Module Bien-être (Wellbeing)

Le module Bien-être est le cœur intelligent de SosI Connect, intégrant des technologies de pointe pour le suivi de la santé :

*   **🧠 Algorithme de Stress Prédictif** : Un moteur d'analyse pondéré qui traite 10 indicateurs (sommeil, anxiété, environnement) pour calculer un score de confiance en temps réel.
*   **📷 Vision IA - Analyse Nutritionnelle** : Utilisation de modèles Vision AI (Qwen 2.5 VL) pour analyser les photos de repas et extraire automatiquement les calories, protéines et glucides.
*   **📊 Dashboard Analytique Dynamique** : Visualisation des tendances de santé via des graphiques interactifs (AreaChart, BarChart) avec gestion des tooltips.
*   **💬 Assistant IA Personnalisé** : Un chatbot intelligent qui analyse votre historique de bien-être pour vous fournir des recommandations sur mesure.

---

## 🧪 Tests & Qualité Logicielle

Le projet intègre une suite de tests automatisés rigoureuse avec **JUnit 5** pour garantir la fiabilité du module Bien-être :

| Type de Test | Objectif | Cible |
| :--- | :--- | :--- |
| **Tests Unitaires** | Validation des calculs IA et cas limites | `StressPredictionService` |
| **Tests CRUD** | Vérification de la persistance SQL | `MealService`, `WellbeingService` |
| **Tests de Sécurité** | **Isolation des données** (User Isolation) | Intégration Sécurité |
| **Tests de Filtrage** | Logique de recherche et tranches caloriques | `MealService.filterMeals` |

> [!TIP]
> Pour exécuter les tests : Faites un clic droit sur le dossier `src/test/java` dans IntelliJ et sélectionnez **"Run 'All Tests'"**.

---

=======
>>>>>>> afab8be (Initial commit - aide et don module)
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
