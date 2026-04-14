package org.example.aideEtdon.frontoffice.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.example.aideEtdon.model.ContactUrgence;
import org.example.aideEtdon.service.ContactUrgenceService;

import java.io.IOException;
import java.util.List;

public class ContactFormController {

    // View Components
    @FXML private VBox contactsContainer;
    
    // Form Components
    @FXML private Label formTitleLabel;
    @FXML private TextField fldNom;
    @FXML private TextField fldEmail;
    @FXML private TextField fldTel;
    @FXML private Label lblError;
    @FXML private Button btnSave;
    @FXML private Button btnCancelEdit;

    private ContactUrgenceService service = new ContactUrgenceService();
    private ContactUrgence currentEditingContact = null;

    @FXML
    public void initialize() {
        loadContacts();
    }

    private void loadContacts() {
        contactsContainer.getChildren().clear();
        List<ContactUrgence> list = service.afficherToutes();

        if (list.isEmpty()) {
            Label empty = new Label("Aucun contact enregistré.\nVeuillez en ajouter un à l'aide du formulaire.");
            empty.setStyle("-fx-font-size: 16px; -fx-text-fill: #94a3b8;");
            contactsContainer.getChildren().add(empty);
            return;
        }

        for (ContactUrgence contact : list) {
            HBox card = new HBox(15);
            card.setStyle("-fx-background-color: #f8fafc; -fx-padding: 15; -fx-background-radius: 10; -fx-border-color: #e2e8f0; -fx-border-radius: 10;");
            
            VBox infoBox = new VBox(5);
            Label nameLbl = new Label("👤 " + contact.getNom());
            nameLbl.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
            
            Label emailLbl = new Label("📧 " + contact.getEmail());
            emailLbl.setStyle("-fx-font-size: 14px; -fx-text-fill: #64748b;");
            
            Label telLbl = new Label("📞 " + (contact.getTelephone() == null || contact.getTelephone().isEmpty() ? "Non renseigné" : contact.getTelephone()));
            telLbl.setStyle("-fx-font-size: 14px; -fx-text-fill: #64748b;");

            infoBox.getChildren().addAll(nameLbl, emailLbl, telLbl);

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Button btnModif = new Button("Modifier");
            btnModif.setStyle("-fx-background-color: #f59e0b; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
            btnModif.setOnAction(e -> triggerEdit(contact));

            Button btnDel = new Button("Supprimer");
            btnDel.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
            btnDel.setOnAction(e -> {
                service.supprimer(contact.getId());
                if (currentEditingContact != null && currentEditingContact.getId() == contact.getId()) {
                    cancelEdit();
                }
                loadContacts();
            });

            card.getChildren().addAll(infoBox, spacer, btnModif, btnDel);
            contactsContainer.getChildren().add(card);
        }
    }

    private void triggerEdit(ContactUrgence contact) {
        currentEditingContact = contact;
        formTitleLabel.setText("Modifier le contact");
        btnSave.setText("💾 METTRE A JOUR");
        btnCancelEdit.setVisible(true);

        fldNom.setText(contact.getNom());
        fldEmail.setText(contact.getEmail());
        fldTel.setText(contact.getTelephone() != null ? contact.getTelephone() : "");
    }

    @FXML
    private void cancelEdit() {
        currentEditingContact = null;
        formTitleLabel.setText("Ajouter un contact");
        btnSave.setText("💾 ENREGISTRER");
        btnCancelEdit.setVisible(false);

        fldNom.clear();
        fldEmail.clear();
        fldTel.clear();
    }

    @FXML
    private void handleSave() {
        lblError.setVisible(false);
        String nom = fldNom.getText().trim();
        String email = fldEmail.getText().trim();
        String tel = fldTel.getText().trim();

        if (nom.isEmpty() || email.isEmpty()) {
            lblError.setVisible(true);
            return;
        }

        if (currentEditingContact == null) {
            // Add new
            service.enregistrer(new ContactUrgence(nom, email, tel));
        } else {
            // Update existing
            currentEditingContact.setNom(nom);
            currentEditingContact.setEmail(email);
            currentEditingContact.setTelephone(tel);
            service.modifier(currentEditingContact);
            cancelEdit();
        }

        loadContacts();
        fldNom.clear();
        fldEmail.clear();
        fldTel.clear();
    }

    @FXML
    private void goBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/aideEtdon/frontoffice/AideHomeView.fxml"));
            Node homeView = loader.load();
            AideEtdonControllerClientController.getInstance().setView(homeView);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
