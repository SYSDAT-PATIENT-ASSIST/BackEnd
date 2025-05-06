package dk.patientassist.persistence.dao;

import java.util.List;
import java.util.Optional;

public interface IDAO<T, ID> {
    Optional<T> get(ID id);
    List<T> getAll();
    T create(T dto);
    T update(ID id, T dto);
    boolean delete(ID id);
}
