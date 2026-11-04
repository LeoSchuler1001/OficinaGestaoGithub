package oficina.dao;

import oficina.entity.Carro;
import jakarta.persistence.*;
import java.util.List;

public class CarroDao {
    private static final EntityManagerFactory EMF = Persistence.createEntityManagerFactory("PU_Oficina");

    public void salvar(Carro carro) {
        EntityManager em = EMF.createEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(carro);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        } finally { em.close(); }
    }

    public void atualizar(Carro carro) {
        EntityManager em = EMF.createEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(carro);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        } finally { em.close(); }
    }

    public void remover(Long id) {
        EntityManager em = EMF.createEntityManager();
        try {
            em.getTransaction().begin();
            Carro carro = em.find(Carro.class, id);
            if (carro != null) {
                em.remove(carro);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        } finally { em.close(); }
    }

    public List<Carro> listarTodos() {
        EntityManager em = EMF.createEntityManager();
        try {
            return em.createQuery("SELECT c FROM Carro c", Carro.class).getResultList();
        } finally { em.close(); }
    }
}