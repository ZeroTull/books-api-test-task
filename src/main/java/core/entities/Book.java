package core.entities;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class Book {
    private Integer id;
    private String name;
    private String author;
    private String publication;
    private String category;
    private String pages;
    private String price;

    @Override
    public Book clone() {
        Book b = new Book();
        b.setId(this.getId())
                .setName(this.getName())
                .setAuthor(this.getAuthor())
                .setPublication(this.getPublication())
                .setCategory(this.getCategory())
                .setPages(this.getPages())
                .setPrice(this.getPrice());
        return b;
    }
}