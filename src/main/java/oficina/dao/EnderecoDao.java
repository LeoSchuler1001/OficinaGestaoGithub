package oficina.dao;

import oficina.entity.Endereco;
import jakarta.persistence.*;
import java.util.List;

public class EnderecoDao {
    private static final EntityManagerFactory EMF = Persistence.createEntityManagerFactory("PU_Oficina");

    public void salvar(Endereco endereco) {
        EntityManager em = EMF.createEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(endereco);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public void atualizar(Endereco endereco) {
        EntityManager em = EMF.createEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(endereco);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public void remover(Long id) {
        EntityManager em = EMF.createEntityManager();
        try {
            em.getTransaction().begin();
            Endereco endereco = em.find(Endereco.class, id);
            if (endereco != null) {
                em.remove(endereco);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public List<Endereco> listarTodos() {
        EntityManager em = EMF.createEntityManager();
        try {
            return em.createQuery("SELECT e FROM Endereco e", Endereco.class).getResultList();
        } finally {
            em.close();
        }
    }
}

