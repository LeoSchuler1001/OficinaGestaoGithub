package oficina.dao;

import oficina.entity.Funcionario;
import jakarta.persistence.*;
import java.util.List;

public class FuncionarioDao {
    private static final EntityManagerFactory EMF = Persistence.createEntityManagerFactory("PU_Oficina");

    public void salvar(Funcionario funcionario) {
        EntityManager em = EMF.createEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(funcionario);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        } finally { em.close(); }
    }

    public void atualizar(Funcionario funcionario) {
        EntityManager em = EMF.createEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(funcionario);
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
            Funcionario funcionario = em.find(Funcionario.class, id);
            if (funcionario != null) {
                em.remove(funcionario);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        } finally { em.close(); }
    }

    public List<Funcionario> listarTodos() {
        EntityManager em = EMF.createEntityManager();
        try {
            return em.createQuery("SELECT f FROM Funcionario f", Funcionario.class).getResultList();
        } finally { em.close(); }
    }
}