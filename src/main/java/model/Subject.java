package model;

public class Subject {
    private int Id;
    private String Name;

    public Subject(){}

    public Subject(int id, String Name) {
        this.Id = id;
        this.Name = Name;
    }

    public int getId() {
        return Id;
    }

    public void setId(int id) {
        this.Id = id;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    @Override
    public String toString() {
        return "Subject{" + "id=" + Id + "} ='" + Name + '\'';
    }
}
