package local.cheema.imageviewer.Model;

import java.io.Serializable;


public class Image implements Serializable{
    private String name;
    private String imageUrl;

    public Image() {
    }

    public Image(String name, String imageUrl) {
        this.name = name;
        this.imageUrl = imageUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImage(String imageUrl) {
        this.imageUrl = imageUrl;
    }


}
