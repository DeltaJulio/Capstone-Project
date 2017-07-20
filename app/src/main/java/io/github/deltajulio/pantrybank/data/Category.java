package io.github.deltajulio.pantrybank.data;

/**
 * TODO: add a class header comment
 */

public class Category
{
    public static final String NAME = "name";
    public static final String ID = "categoryId";

    private String name;
    private String categoryId;

    @SuppressWarnings("unused")
    public Category() { /*Needed for Firebase ui*/ }

    public Category(String name, String categoryId)
    {
        setName(name);
        setCategoryId(categoryId);
    }

    public Category(String name)
    {
        setName(name);
    }

    public final String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public final String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }
}
