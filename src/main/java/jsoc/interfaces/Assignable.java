package jsoc.interfaces;

public interface Assignable<U> {

    void assignTo(U user);

    void unassign();

    U getAssignee();

    boolean isAssigned();
}
