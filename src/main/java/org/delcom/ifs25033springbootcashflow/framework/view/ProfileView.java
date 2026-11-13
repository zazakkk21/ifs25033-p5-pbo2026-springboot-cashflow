package org.delcom.ifs25033springbootcashflow.framework.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.delcom.ifs25033springbootcashflow.adapter.storage.FileStorage;
import org.delcom.ifs25033springbootcashflow.domain.entity.User;
import org.delcom.ifs25033springbootcashflow.domain.exception.DomainException;
import org.delcom.ifs25033springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs25033springbootcashflow.framework.auth.AuthSession;
import org.delcom.ifs25033springbootcashflow.usecase.UserUseCase;

import java.io.IOException;

@Route(value = "profile", layout = MainLayout.class)
@PageTitle("Profil | Cashflow")
public class ProfileView extends VerticalLayout {
    private final UserUseCase userUseCase;
    private final FileStorage fileStorage;
    private final User user;

    public ProfileView(UserUseCase userUseCase, FileStorage fileStorage, IUserRepository userRepository) {
        this.userUseCase = userUseCase;
        this.fileStorage = fileStorage;
        this.user = AuthSession.currentUser(userRepository).orElseThrow();
        addClassName("content-page");
        setPadding(true);
        add(new H2("Profil pengguna"));
        TextField username = new TextField("Username");
        username.setValue(user.getUsername());
        username.setReadOnly(true);
        TextField fullName = new TextField("Nama lengkap");
        fullName.setValue(user.getFullName());
        fullName.setMaxLength(200);
        PasswordField password = new PasswordField("Password baru (opsional)");
        if (user.getPhotoPath() != null) {
            Image photo = new Image("/uploads/" + user.getPhotoPath(), "Foto profil");
            photo.setWidth("120px");
            photo.setHeight("120px");
            photo.getStyle().set("object-fit", "cover").set("border-radius", "50%");
            add(photo);
        }
        MemoryBuffer buffer = new MemoryBuffer();
        Upload upload = new Upload(buffer);
        upload.setAcceptedFileTypes("image/png", "image/jpeg", "image/gif", "image/webp");
        upload.setMaxFiles(1);
        upload.setMaxFileSize(5 * 1024 * 1024);
        Button save = new Button("Simpan profil", event -> saveProfile(fullName.getValue(), password.getValue()));
        save.addClassName("action-income");
        add(username, fullName, password, upload, save);
        upload.addSucceededListener(event -> {
            String newPath = null;
            String oldPath = user.getPhotoPath();
            try {
                newPath = fileStorage.store(buffer.getInputStream(), buffer.getFileName());
                userUseCase.updatePhoto(user.getId(), newPath);
                AuthSession.refresh(user);
                Notification.show("Foto profil berhasil diperbarui");
                if (oldPath != null) {
                    try {
                        fileStorage.delete(oldPath);
                    } catch (IOException exception) {
                        Notification.show("Foto diperbarui, tetapi foto lama gagal dihapus");
                    }
                }
                getUI().ifPresent(ui -> ui.getPage().reload());
            } catch (IOException | DomainException | IllegalArgumentException exception) {
                if (newPath != null) {
                    try {
                        fileStorage.delete(newPath);
                    } catch (IOException cleanupException) {
                        exception.addSuppressed(cleanupException);
                    }
                }
                Notification.show("Gagal memperbarui foto: " + exception.getMessage());
            }
        });
    }

    private void saveProfile(String fullName, String password) {
        try {
            User updated = userUseCase.updateProfile(user.getId(), fullName, password);
            AuthSession.refresh(updated);
            Notification.show("Profil berhasil diperbarui");
        } catch (DomainException exception) {
            Notification.show(exception.getMessage());
        }
    }
}
