package oficina.dao;

import oficina.entity.Saida;
import jakarta.persistence.*;
import java.util.List;

public class SaidaDao {
    private static final EntityManagerFactory EMF = Persistence.createEntityManagerFactory("PU_Oficina");

    public void salvar(Saida saida) {
        EntityManager em = EMF.createEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(saida);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public void atualizar(Saida saida) {
        EntityManager em = EMF.createEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(saida);
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
            Saida saida = em.find(Saida.class, id);
            if (saida != null) {
                em.remove(saida);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public List<Saida> listarTodos() {
        EntityManager em = EMF.createEntityManager();
        try {
            return em.createQuery("SELECT s FROM Saida s ORDER BY s.id DESC", Saida.class).getResultList();
        } finally {
            em.close();
        }
    }
}

