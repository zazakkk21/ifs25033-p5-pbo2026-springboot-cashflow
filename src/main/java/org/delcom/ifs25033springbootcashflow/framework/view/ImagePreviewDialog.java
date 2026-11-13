package org.delcom.ifs25033springbootcashflow.framework.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public class ImagePreviewDialog extends Dialog {
    private boolean zoomed;

    public ImagePreviewDialog(String path) {
        setHeaderTitle("Pratinjau gambar");
        Image image = new Image(path, "Pratinjau");
        image.getStyle().set("max-width", "80vw").set("max-height", "70vh").set("object-fit", "contain");
        Button zoom = new Button("Perbesar");
        zoom.addClickListener(event -> toggleZoom(image, zoom));
        Button imageButton = new Button(image);
        imageButton.addClickListener(event -> toggleZoom(image, zoom));
        Button close = new Button("Tutup", event -> close());
        HorizontalLayout footer = new HorizontalLayout(zoom, close);
        footer.setWidthFull();
        footer.setJustifyContentMode(HorizontalLayout.JustifyContentMode.END);
        VerticalLayout content = new VerticalLayout(imageButton);
        content.setAlignItems(VerticalLayout.Alignment.CENTER);
        add(content);
        getFooter().add(footer);
    }

    private void toggleZoom(Image image, Button zoom) {
        zoomed = !zoomed;
        image.getStyle().set("max-width", zoomed ? "95vw" : "80vw");
        image.getStyle().set("max-height", zoomed ? "85vh" : "70vh");
        zoom.setText(zoomed ? "Perkecil" : "Perbesar");
    }
}
