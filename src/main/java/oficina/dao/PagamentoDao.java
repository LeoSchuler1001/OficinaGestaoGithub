package oficina.dao;

import oficina.entity.Pagamento;
import jakarta.persistence.*;
import java.util.List;

public class PagamentoDao {
    private static final EntityManagerFactory EMF = Persistence.createEntityManagerFactory("PU_Oficina");

    public void salvar(Pagamento pagamento) {
        EntityManager em = EMF.createEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(pagamento);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        } finally { em.close(); }
    }

    public void atualizar(Pagamento pagamento) {
        EntityManager em = EntityManagerFactory.class.isAssignableFrom(EntityManager.class) ? null : EMF.createEntityManager(); // Simplificado abaixo:
        EntityManager em2 = EMF.createEntityManager();
        try {
            em2.getTransaction().begin();
            em2.merge(pagamento);
            em2.getTransaction().commit();
        } catch (Exception e) {
            em2.getTransaction().rollback();
            throw e;
        } finally { em2.close(); }
    }

    public void remover(Long id) {
        EntityManager em = EMF.createEntityManager();
        try {
            em.getTransaction().begin();
            Pagamento pagamento = em.find(Pagamento.class, id);
            if (pagamento != null) {
                em.remove(pagamento);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        } finally { em.close(); }
    }

    public List<Pagamento> listarTodos() {
        EntityManager em = EMF.createEntityManager();
        try {
            return em.createQuery("SELECT p FROM Pagamento p", Pagamento.class).getResultList();
        } finally { em.close(); }
    }
}