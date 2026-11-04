package oficina.view;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import oficina.dao.*;
import oficina.entity.*;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainApp extends Application {

    private final ClienteDao clienteDao = new ClienteDao();
    private final CarroDao carroDao = new CarroDao();
    private final FuncionarioDao funcionarioDao = new FuncionarioDao();
    private final PagamentoDao pagamentoDao = new PagamentoDao();
    private final ContaDao contaDao = new ContaDao();
    private final SaidaDao saidaDao = new SaidaDao();
    private final EnderecoDao enderecoDao = new EnderecoDao();

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Sistema de Gestão - Oficina Mecânica");

        TabPane tabPane = new TabPane();
        tabPane.getTabs().addAll(
            criarAbaClientes(),
            criarAbaCarros(),
            criarAbaFuncionarios(),
            criarAbaPagamentos(),
            criarAbaContas()
        );

        Scene scene = new Scene(tabPane, 960, 640);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // --- ABA CLIENTES ---
    private Tab criarAbaClientes() {
        Tab tab = new Tab("Clientes");
        tab.setClosable(false);
        GridPane grid = criarGrid();
        TextField txtId = new TextField();
        txtId.setVisible(false);
        TextField txtNome = new TextField();
        txtNome.setPromptText("Nome completo do cliente");
        TextField txtTel = new TextField();
        txtTel.setPromptText("Ex: (11) 98765-4321");
        TextField txtRua = new TextField();
        txtRua.setPromptText("Ex: Av. Paulista, Rua das Flores");
        TextField txtNum = new TextField();
        txtNum.setPromptText("Ex: 123, 45-B");
        TextField txtBairro = new TextField();
        txtBairro.setPromptText("Ex: Centro, Bela Vista");
        TextField txtCidade = new TextField();
        txtCidade.setPromptText("Ex: São Paulo");
        TextField txtCep = new TextField();
        txtCep.setPromptText("Ex: 01310-100");

        Button btnSalvar = new Button("Salvar / Atualizar Cliente");
        Button btnLimpar = new Button("Limpar");
        Button btnExcluir = new Button("Excluir Selecionado");
        Label lblMsg = new Label();

        TableView<Cliente> tabela = new TableView<>();
        tabela.setPrefHeight(290);

        TableColumn<Cliente, Long> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colId.setPrefWidth(45);
        colId.setResizable(false);

        TableColumn<Cliente, String> colNome = new TableColumn<>("Nome");
        colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colNome.setPrefWidth(180);
        colNome.setResizable(false);

        TableColumn<Cliente, String> colTel = new TableColumn<>("Telefone");
        colTel.setCellValueFactory(new PropertyValueFactory<>("telefone"));
        colTel.setPrefWidth(120);
        colTel.setResizable(false);

        TableColumn<Cliente, String> colEndereco = new TableColumn<>("Endereço");
        colEndereco.setCellValueFactory(cellData -> {
            Cliente c = cellData.getValue();
            String texto = (c.getEndereco() != null) ? c.getEndereco().toString() : "Não informado";
            return new javafx.beans.property.SimpleStringProperty(texto);
        });
        colEndereco.setPrefWidth(540);
        colEndereco.setResizable(false);

        tabela.getColumns().addAll(colId, colNome, colTel, colEndereco);
        atualizarTabelaClientes(tabela);

        tabela.setOnMouseClicked(e -> {
            Cliente selecionado = tabela.getSelectionModel().getSelectedItem();
            if (selecionado != null) {
                txtId.setText(selecionado.getId().toString());
                txtNome.setText(selecionado.getNome());
                txtTel.setText(selecionado.getTelefone());
                Endereco end = selecionado.getEndereco();
                if (end != null) {
                    txtRua.setText(end.getRua() != null ? end.getRua() : "");
                    txtNum.setText(end.getNumero() != null ? end.getNumero() : "");
                    txtBairro.setText(end.getBairro() != null ? end.getBairro() : "");
                    txtCidade.setText(end.getCidade() != null ? end.getCidade() : "");
                    txtCep.setText(end.getCep() != null ? end.getCep() : "");
                } else {
                    txtRua.clear(); txtNum.clear(); txtBairro.clear(); txtCidade.clear(); txtCep.clear();
                }
            }
        });

        grid.add(new Label("Nome:"), 0, 0); grid.add(txtNome, 1, 0);
        grid.add(new Label("Telefone:"), 2, 0); grid.add(txtTel, 3, 0);

        grid.add(new Label("Rua / Av:"), 0, 1); grid.add(txtRua, 1, 1);
        grid.add(new Label("Número:"), 2, 1); grid.add(txtNum, 3, 1);

        grid.add(new Label("Bairro:"), 0, 2); grid.add(txtBairro, 1, 2);
        grid.add(new Label("Cidade:"), 2, 2); grid.add(txtCidade, 3, 2);

        grid.add(new Label("CEP:"), 0, 3); grid.add(txtCep, 1, 3);

        HBox botoes = new HBox(10, btnSalvar, btnLimpar, btnExcluir);
        grid.add(botoes, 1, 4, 3, 1);
        grid.add(lblMsg, 1, 5, 3, 1);

        btnSalvar.setOnAction(e -> {
            try {
                if (txtNome.getText().trim().isEmpty() || txtTel.getText().trim().isEmpty()) {
                    lblMsg.setText("Preencha ao menos o nome e o telefone do cliente!");
                    lblMsg.setStyle("-fx-text-fill: red;");
                    return;
                }

                boolean temEndereco = !txtRua.getText().trim().isEmpty() ||
                                      !txtBairro.getText().trim().isEmpty() ||
                                      !txtCidade.getText().trim().isEmpty();

                if (txtId.getText().isEmpty()) {
                    Cliente c = new Cliente(txtNome.getText().trim(), txtTel.getText().trim());
                    if (temEndereco) {
                        Endereco end = new Endereco(
                            txtRua.getText().trim(),
                            txtNum.getText().trim().isEmpty() ? "S/N" : txtNum.getText().trim(),
                            txtBairro.getText().trim(),
                            txtCidade.getText().trim(),
                            txtCep.getText().trim()
                        );
                        c.setEndereco(end);
                    }
                    clienteDao.salvar(c);
                    lblMsg.setText("Cliente e endereço cadastrados com sucesso!");
                } else {
                    Long id = Long.parseLong(txtId.getText());
                    Cliente c = clienteDao.listarTodos().stream().filter(x -> x.getId().equals(id)).findFirst().orElse(null);
                    if (c != null) {
                        c.setNome(txtNome.getText().trim());
                        c.setTelefone(txtTel.getText().trim());
                        if (temEndereco) {
                            if (c.getEndereco() != null) {
                                c.getEndereco().setRua(txtRua.getText().trim());
                                c.getEndereco().setNumero(txtNum.getText().trim().isEmpty() ? "S/N" : txtNum.getText().trim());
                                c.getEndereco().setBairro(txtBairro.getText().trim());
                                c.getEndereco().setCidade(txtCidade.getText().trim());
                                c.getEndereco().setCep(txtCep.getText().trim());
                            } else {
                                Endereco end = new Endereco(
                                    txtRua.getText().trim(),
                                    txtNum.getText().trim().isEmpty() ? "S/N" : txtNum.getText().trim(),
                                    txtBairro.getText().trim(),
                                    txtCidade.getText().trim(),
                                    txtCep.getText().trim()
                                );
                                c.setEndereco(end);
                            }
                        }
                        clienteDao.atualizar(c);
                        lblMsg.setText("Cliente e endereço atualizados com sucesso!");
                    }
                }
                lblMsg.setStyle("-fx-text-fill: green;");
                limparCamposCliente(txtId, txtNome, txtTel, txtRua, txtNum, txtBairro, txtCidade, txtCep);
                atualizarTabelaClientes(tabela);
            } catch (Exception ex) {
                lblMsg.setText("Erro: " + ex.getMessage());
                lblMsg.setStyle("-fx-text-fill: red;");
            }
        });

        btnLimpar.setOnAction(e -> limparCamposCliente(txtId, txtNome, txtTel, txtRua, txtNum, txtBairro, txtCidade, txtCep));

        btnExcluir.setOnAction(e -> {
            Cliente selecionado = tabela.getSelectionModel().getSelectedItem();
            if (selecionado != null) {
                try {
                    clienteDao.remover(selecionado.getId());
                    lblMsg.setText("Cliente e endereço excluídos com sucesso!");
                    lblMsg.setStyle("-fx-text-fill: green;");
                    limparCamposCliente(txtId, txtNome, txtTel, txtRua, txtNum, txtBairro, txtCidade, txtCep);
                    atualizarTabelaClientes(tabela);
                } catch (Exception ex) {
                    lblMsg.setText("Erro ao excluir (possui carros vinculados).");
                    lblMsg.setStyle("-fx-text-fill: red;");
                }
            }
        });

        VBox vbox = new VBox(15, grid, tabela);
        vbox.setPadding(new Insets(15));
        tab.setContent(vbox);
        return tab;
    }

    private void atualizarTabelaClientes(TableView<Cliente> tabela) {
        tabela.setItems(FXCollections.observableArrayList(clienteDao.listarTodos()));
    }

    private void limparCamposCliente(TextField id, TextField nome, TextField tel,
                                     TextField rua, TextField num, TextField bairro, TextField cidade, TextField cep) {
        id.clear(); nome.clear(); tel.clear();
        rua.clear(); num.clear(); bairro.clear(); cidade.clear(); cep.clear();
    }

    // --- ABA CARROS ---
    private Tab criarAbaCarros() {
        Tab tab = new Tab("Carros");
        tab.setClosable(false);

        GridPane grid = criarGrid();
        TextField txtId = new TextField();
        txtId.setVisible(false);
        TextField txtModelo = new TextField();
        TextField txtMarca = new TextField();
        TextField txtPlaca = new TextField();
        ComboBox<Cliente> cbClientes = new ComboBox<>();
        
        configurarComboBoxCliente(cbClientes);

        Button btnSalvar = new Button("Salvar / Atualizar Carro");
        Button btnLimpar = new Button("Limpar");
        Button btnExcluir = new Button("Excluir Selecionado");
        Label lblMsg = new Label();

        TableView<Carro> tabela = new TableView<>();
        tabela.setPrefHeight(300);

        TableColumn<Carro, Long> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colId.setPrefWidth(50);
        colId.setResizable(false);

        TableColumn<Carro, String> colMarca = new TableColumn<>("Marca");
        colMarca.setCellValueFactory(new PropertyValueFactory<>("marca"));
        colMarca.setPrefWidth(140);
        colMarca.setResizable(false);

        TableColumn<Carro, String> colModelo = new TableColumn<>("Modelo");
        colModelo.setCellValueFactory(new PropertyValueFactory<>("modelo"));
        colModelo.setPrefWidth(140);
        colModelo.setResizable(false);

        TableColumn<Carro, String> colPlaca = new TableColumn<>("Placa");
        colPlaca.setCellValueFactory(new PropertyValueFactory<>("placa"));
        colPlaca.setPrefWidth(110);
        colPlaca.setResizable(false);

        TableColumn<Carro, Cliente> colCliente = new TableColumn<>("Cliente");
        colCliente.setCellValueFactory(new PropertyValueFactory<>("cliente"));
        colCliente.setCellFactory(column -> new TableCell<Carro, Cliente>() {
            @Override
            protected void updateItem(Cliente cliente, boolean empty) {
                super.updateItem(cliente, empty);
                setText(empty || cliente == null ? null : cliente.getNome());
            }
        });
        colCliente.setPrefWidth(240);
        colCliente.setResizable(false);

        TableColumn<Carro, Cliente> colTelefone = new TableColumn<>("Tel. Cliente");
        colTelefone.setCellValueFactory(new PropertyValueFactory<>("cliente"));
        colTelefone.setCellFactory(column -> new TableCell<Carro, Cliente>() {
            @Override
            protected void updateItem(Cliente cliente, boolean empty) {
                super.updateItem(cliente, empty);
                setText(empty || cliente == null ? null : cliente.getTelefone());
            }
        });
        colTelefone.setPrefWidth(160);
        colTelefone.setResizable(false);

        tabela.getColumns().addAll(colId, colMarca, colModelo, colPlaca, colCliente, colTelefone);

        tab.setOnSelectionChanged(e -> {
            if (tab.isSelected()) {
                cbClientes.setItems(FXCollections.observableArrayList(clienteDao.listarTodos()));
                tabela.setItems(FXCollections.observableArrayList(carroDao.listarTodos()));
            }
        });

        tabela.setOnMouseClicked(e -> {
            Carro sel = tabela.getSelectionModel().getSelectedItem();
            if (sel != null) {
                txtId.setText(sel.getId().toString());
                txtModelo.setText(sel.getModelo());
                txtMarca.setText(sel.getMarca());
                txtPlaca.setText(sel.getPlaca());
                cbClientes.setValue(sel.getCliente());
            }
        });

        grid.add(new Label("Modelo:"), 0, 0); grid.add(txtModelo, 1, 0);
        grid.add(new Label("Marca:"), 0, 1); grid.add(txtMarca, 1, 1);
        grid.add(new Label("Placa:"), 0, 2); grid.add(txtPlaca, 1, 2);
        grid.add(new Label("Cliente:"), 0, 3); grid.add(cbClientes, 1, 3);
        
        HBox botoes = new HBox(10, btnSalvar, btnLimpar, btnExcluir);
        grid.add(botoes, 1, 4);
        grid.add(lblMsg, 1, 5);

        btnSalvar.setOnAction(e -> {
            try {
                if(txtModelo.getText().isEmpty() || txtMarca.getText().isEmpty() || txtPlaca.getText().isEmpty() || cbClientes.getValue() == null) {
                    lblMsg.setText("Preencha todos os campos!");
                    lblMsg.setStyle("-fx-text-fill: red;");
                    return;
                }
                if (txtId.getText().isEmpty()) {
                    Carro c = new Carro(txtModelo.getText(), txtMarca.getText(), txtPlaca.getText(), cbClientes.getValue());
                    carroDao.salvar(c);
                    lblMsg.setText("Carro salvo com sucesso!");
                } else {
                    Carro c = carroDao.listarTodos().stream().filter(x -> x.getId().equals(Long.parseLong(txtId.getText()))).findFirst().orElse(null);
                    if (c != null) {
                        c.setModelo(txtModelo.getText());
                        c.setMarca(txtMarca.getText());
                        c.setPlaca(txtPlaca.getText());
                        c.setCliente(cbClientes.getValue());
                        carroDao.atualizar(c);
                        lblMsg.setText("Carro atualizado com sucesso!");
                    }
                }
                lblMsg.setStyle("-fx-text-fill: green;");
                txtId.clear(); txtModelo.clear(); txtMarca.clear(); txtPlaca.clear();
                cbClientes.setValue(null);
                tabela.setItems(FXCollections.observableArrayList(carroDao.listarTodos()));
            } catch (Exception ex) {
                lblMsg.setText("Erro: " + ex.getMessage());
                lblMsg.setStyle("-fx-text-fill: red;");
            }
        });

        btnLimpar.setOnAction(e -> {
            txtId.clear(); txtModelo.clear(); txtMarca.clear(); txtPlaca.clear();
            cbClientes.setValue(null);
        });

        btnExcluir.setOnAction(e -> {
            Carro sel = tabela.getSelectionModel().getSelectedItem();
            if (sel != null) {
                try {
                    carroDao.remover(sel.getId());
                    tabela.setItems(FXCollections.observableArrayList(carroDao.listarTodos()));
                    lblMsg.setText("Carro excluído com sucesso!");
                    lblMsg.setStyle("-fx-text-fill: green;");
                } catch (Exception ex) {
                    lblMsg.setText("Erro ao excluir (possui pagamentos vinculados).");
                    lblMsg.setStyle("-fx-text-fill: red;");
                }
            }
        });

        VBox vbox = new VBox(15, grid, tabela);
        vbox.setPadding(new Insets(15));
        tab.setContent(vbox);
        return tab;
    }

    private void configurarComboBoxCliente(ComboBox<Cliente> cb) {
        cb.setCellFactory(param -> new ListCell<Cliente>() {
            @Override
            protected void updateItem(Cliente cliente, boolean empty) {
                super.updateItem(cliente, empty);
                setText(empty || cliente == null ? null : cliente.getNome() + " (" + cliente.getTelefone() + ")");
            }
        });
        cb.setConverter(new StringConverter<Cliente>() {
            @Override
            public String toString(Cliente cliente) {
                return cliente == null ? "" : cliente.getNome() + " (" + cliente.getTelefone() + ")";
            }
            @Override
            public Cliente fromString(String string) {
                return null;
            }
        });
    }

    // --- ABA FUNCIONÁRIOS ---
    private Tab criarAbaFuncionarios() {
        Tab tab = new Tab("Funcionários");
        tab.setClosable(false);

        GridPane grid = criarGrid();
        TextField txtId = new TextField();
        txtId.setVisible(false);
        TextField txtNome = new TextField();
        TextField txtCargo = new TextField();
        Button btnSalvar = new Button("Salvar / Atualizar");
        Button btnLimpar = new Button("Limpar");
        Button btnExcluir = new Button("Excluir Selecionado");
        Label lblMsg = new Label();

        TableView<Funcionario> tabela = new TableView<>();
        tabela.setPrefHeight(320);

        TableColumn<Funcionario, Long> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colId.setPrefWidth(60);
        colId.setResizable(false);

        TableColumn<Funcionario, String> colNome = new TableColumn<>("Nome");
        colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colNome.setPrefWidth(480);
        colNome.setResizable(false);

        TableColumn<Funcionario, String> colCargo = new TableColumn<>("Cargo");
        colCargo.setCellValueFactory(new PropertyValueFactory<>("cargo"));
        colCargo.setPrefWidth(300);
        colCargo.setResizable(false);

        tabela.getColumns().addAll(colId, colNome, colCargo);
        tabela.setItems(FXCollections.observableArrayList(funcionarioDao.listarTodos()));

        tabela.setOnMouseClicked(e -> {
            Funcionario f = tabela.getSelectionModel().getSelectedItem();
            if (f != null) {
                txtId.setText(f.getId().toString());
                txtNome.setText(f.getNome());
                txtCargo.setText(f.getCargo());
            }
        });

        grid.add(new Label("Nome:"), 0, 0); grid.add(txtNome, 1, 0);
        grid.add(new Label("Cargo:"), 0, 1); grid.add(txtCargo, 1, 1);
        grid.add(new HBox(10, btnSalvar, btnLimpar, btnExcluir), 1, 2);
        grid.add(lblMsg, 1, 3);

        btnSalvar.setOnAction(e -> {
            try {
                if(txtNome.getText().isEmpty() || txtCargo.getText().isEmpty()) {
                    lblMsg.setText("Preencha todos os campos!");
                    lblMsg.setStyle("-fx-text-fill: red;");
                    return;
                }
                if (txtId.getText().isEmpty()) {
                    funcionarioDao.salvar(new Funcionario(txtNome.getText(), txtCargo.getText()));
                    lblMsg.setText("Funcionário salvo com sucesso!");
                } else {
                    Funcionario f = funcionarioDao.listarTodos().stream().filter(x -> x.getId().equals(Long.parseLong(txtId.getText()))).findFirst().orElse(null);
                    if (f != null) {
                        f.setNome(txtNome.getText());
                        f.setCargo(txtCargo.getText());
                        funcionarioDao.atualizar(f);
                        lblMsg.setText("Funcionário atualizado com sucesso!");
                    }
                }
                lblMsg.setStyle("-fx-text-fill: green;");
                txtId.clear(); txtNome.clear(); txtCargo.clear();
                tabela.setItems(FXCollections.observableArrayList(funcionarioDao.listarTodos()));
            } catch (Exception ex) { 
                lblMsg.setText("Erro: " + ex.getMessage()); 
                lblMsg.setStyle("-fx-text-fill: red;");
            }
        });

        btnLimpar.setOnAction(e -> {
            txtId.clear(); txtNome.clear(); txtCargo.clear();
        });

        btnExcluir.setOnAction(e -> {
            Funcionario f = tabela.getSelectionModel().getSelectedItem();
            if (f != null) {
                try {
                    funcionarioDao.remover(f.getId());
                    tabela.setItems(FXCollections.observableArrayList(funcionarioDao.listarTodos()));
                    lblMsg.setText("Funcionário excluído!");
                    lblMsg.setStyle("-fx-text-fill: green;");
                } catch (Exception ex) {
                    lblMsg.setText("Erro ao excluir (possui pagamentos vinculados).");
                    lblMsg.setStyle("-fx-text-fill: red;");
                }
            }
        });

        VBox vbox = new VBox(15, grid, tabela);
        vbox.setPadding(new Insets(15));
        tab.setContent(vbox);
        return tab;
    }

    // --- ABA PAGAMENTOS (ENTRADAS DE DINHEIRO POR SERVIÇOS EM CARROS) ---
    private Tab criarAbaPagamentos() {
        Tab tab = new Tab("Pagamentos");
        tab.setClosable(false);

        GridPane grid = criarGrid();
        TextField txtId = new TextField();
        txtId.setVisible(false);
        TextField txtValor = new TextField();
        txtValor.setPromptText("Ex: 250.00");
        ComboBox<Carro> cbCarros = new ComboBox<>();
        ComboBox<Funcionario> cbFuncs = new ComboBox<>();
        ComboBox<Conta> cbContas = new ComboBox<>();
        cbContas.setPromptText("Selecione a conta de destino");

        cbCarros.setCellFactory(p -> new ListCell<Carro>() {
            @Override
            protected void updateItem(Carro c, boolean empty) {
                super.updateItem(c, empty);
                setText(empty || c == null ? null : c.getMarca() + " " + c.getModelo() + " [" + c.getPlaca() + "]");
            }
        });
        cbCarros.setConverter(new StringConverter<Carro>() {
            @Override
            public String toString(Carro c) {
                return c == null ? "" : c.getMarca() + " " + c.getModelo() + " [" + c.getPlaca() + "]";
            }
            @Override
            public Carro fromString(String string) { return null; }
        });

        cbFuncs.setCellFactory(p -> new ListCell<Funcionario>() {
            @Override
            protected void updateItem(Funcionario f, boolean empty) {
                super.updateItem(f, empty);
                setText(empty || f == null ? null : f.getNome() + " (" + f.getCargo() + ")");
            }
        });
        cbFuncs.setConverter(new StringConverter<Funcionario>() {
            @Override
            public String toString(Funcionario f) {
                return f == null ? "" : f.getNome() + " (" + f.getCargo() + ")";
            }
            @Override
            public Funcionario fromString(String string) { return null; }
        });

        cbContas.setCellFactory(p -> new ListCell<Conta>() {
            @Override
            protected void updateItem(Conta c, boolean empty) {
                super.updateItem(c, empty);
                setText(empty || c == null ? null : c.getNome() + " [Saldo: R$ " + c.getSaldo() + "]");
            }
        });
        cbContas.setConverter(new StringConverter<Conta>() {
            @Override
            public String toString(Conta c) {
                return c == null ? "" : c.getNome() + " [Saldo: R$ " + c.getSaldo() + "]";
            }
            @Override
            public Conta fromString(String string) { return null; }
        });

        Button btnSalvar = new Button("Salvar / Atualizar Pagamento");
        Button btnLimpar = new Button("Limpar");
        Button btnExcluir = new Button("Excluir Selecionado");
        Label lblMsg = new Label();

        TableView<Pagamento> tabela = new TableView<>();
        tabela.setPrefHeight(270);

        TableColumn<Pagamento, Long> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colId.setPrefWidth(45);
        colId.setResizable(false);

        TableColumn<Pagamento, BigDecimal> colValor = new TableColumn<>("Valor (R$)");
        colValor.setCellValueFactory(new PropertyValueFactory<>("valor"));
        colValor.setPrefWidth(95);
        colValor.setResizable(false);

        TableColumn<Pagamento, Carro> colCarro = new TableColumn<>("Carro");
        colCarro.setCellValueFactory(new PropertyValueFactory<>("carro"));
        colCarro.setCellFactory(c -> new TableCell<Pagamento, Carro>() {
            @Override
            protected void updateItem(Carro carro, boolean empty) {
                super.updateItem(carro, empty);
                setText(empty || carro == null ? null : carro.getMarca() + " " + carro.getModelo() + " (" + carro.getPlaca() + ")");
            }
        });
        colCarro.setPrefWidth(190);
        colCarro.setResizable(false);

        TableColumn<Pagamento, Date> colData = new TableColumn<>("Data");
        colData.setCellValueFactory(new PropertyValueFactory<>("dataPagamento"));
        colData.setCellFactory(column -> new TableCell<Pagamento, Date>() {
            private final SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            @Override
            protected void updateItem(Date data, boolean empty) {
                super.updateItem(data, empty);
                setText(empty || data == null ? null : formato.format(data));
            }
        });
        colData.setPrefWidth(130);
        colData.setResizable(false);

        TableColumn<Pagamento, Funcionario> colFunc = new TableColumn<>("Funcionário Recebeu");
        colFunc.setCellValueFactory(new PropertyValueFactory<>("funcionario"));
        colFunc.setCellFactory(f -> new TableCell<Pagamento, Funcionario>() {
            @Override
            protected void updateItem(Funcionario func, boolean empty) {
                super.updateItem(func, empty);
                setText(empty || func == null ? null : func.getNome());
            }
        });
        colFunc.setPrefWidth(160);
        colFunc.setResizable(false);

        TableColumn<Pagamento, Conta> colConta = new TableColumn<>("Conta de Entrada");
        colConta.setCellValueFactory(new PropertyValueFactory<>("conta"));
        colConta.setCellFactory(c -> new TableCell<Pagamento, Conta>() {
            @Override
            protected void updateItem(Conta conta, boolean empty) {
                super.updateItem(conta, empty);
                setText(empty || conta == null ? "-" : conta.getNome());
            }
        });
        colConta.setPrefWidth(190);
        colConta.setResizable(false);

        tabela.getColumns().addAll(colId, colValor, colCarro, colData, colFunc, colConta);

        Runnable recarregarListas = () -> {
            try {
                cbCarros.setItems(FXCollections.observableArrayList(carroDao.listarTodos()));
                cbFuncs.setItems(FXCollections.observableArrayList(funcionarioDao.listarTodos()));
                cbContas.setItems(FXCollections.observableArrayList(contaDao.listarTodos()));
                tabela.setItems(FXCollections.observableArrayList(pagamentoDao.listarTodos()));
            } catch (Exception ex) {
                lblMsg.setText("Aviso ao carregar: " + ex.getMessage());
                lblMsg.setStyle("-fx-text-fill: red;");
            }
        };

        tab.setOnSelectionChanged(e -> {
            if (tab.isSelected()) {
                recarregarListas.run();
            }
        });

        tabela.setOnMouseClicked(e -> {
            Pagamento p = tabela.getSelectionModel().getSelectedItem();
            if (p != null) {
                txtId.setText(p.getId().toString());
                txtValor.setText(p.getValor().toString());
                cbCarros.setValue(p.getCarro());
                cbFuncs.setValue(p.getFuncionario());
                cbContas.setValue(p.getConta());
            }
        });

        grid.add(new Label("Valor (R$):"), 0, 0); grid.add(txtValor, 1, 0);
        grid.add(new Label("Carro (Serviço):"), 0, 1); grid.add(cbCarros, 1, 1);
        grid.add(new Label("Funcionário Recebeu:"), 0, 2); grid.add(cbFuncs, 1, 2);
        grid.add(new Label("Conta que vai receber:"), 0, 3); grid.add(cbContas, 1, 3);
        
        HBox botoes = new HBox(10, btnSalvar, btnLimpar, btnExcluir);
        grid.add(botoes, 1, 4);
        grid.add(lblMsg, 1, 5);

        btnSalvar.setOnAction(e -> {
            try {
                if (txtValor.getText().trim().isEmpty() || cbCarros.getValue() == null ||
                    cbFuncs.getValue() == null || cbContas.getValue() == null) {
                    lblMsg.setText("Preencha todos os campos e selecione a conta de destino!");
                    lblMsg.setStyle("-fx-text-fill: red;");
                    return;
                }

                BigDecimal valor = new BigDecimal(txtValor.getText().trim().replace(",", "."));
                Conta contaDestino = cbContas.getValue();

                if (txtId.getText().isEmpty()) {
                    Pagamento p = new Pagamento(valor, cbCarros.getValue(), cbFuncs.getValue(), contaDestino);
                    pagamentoDao.salvar(p);

                    contaDestino.setSaldo(contaDestino.getSaldo().add(valor));
                    contaDao.atualizar(contaDestino);

                    lblMsg.setText("Pagamento registrado! Entrada de R$ " + valor + " na conta " + contaDestino.getNome());
                } else {
                    Long idPagamento = Long.parseLong(txtId.getText());
                    Pagamento p = pagamentoDao.listarTodos().stream()
                        .filter(x -> x.getId().equals(idPagamento)).findFirst().orElse(null);

                    if (p != null) {
                        Conta contaAntiga = p.getConta();
                        BigDecimal valorAntigo = p.getValor();

                        if (contaAntiga != null) {
                            contaAntiga.setSaldo(contaAntiga.getSaldo().subtract(valorAntigo));
                            contaDao.atualizar(contaAntiga);
                        }

                        Conta contaAtual = contaDao.listarTodos().stream()
                            .filter(c -> c.getId().equals(contaDestino.getId())).findFirst().orElse(contaDestino);
                        contaAtual.setSaldo(contaAtual.getSaldo().add(valor));
                        contaDao.atualizar(contaAtual);

                        p.setValor(valor);
                        p.setCarro(cbCarros.getValue());
                        p.setFuncionario(cbFuncs.getValue());
                        p.setConta(contaAtual);
                        pagamentoDao.atualizar(p);

                        lblMsg.setText("Pagamento atualizado e saldo recalculado!");
                    }
                }

                lblMsg.setStyle("-fx-text-fill: green;");
                txtId.clear(); txtValor.clear(); cbCarros.setValue(null); cbFuncs.setValue(null); cbContas.setValue(null);
                recarregarListas.run();
            } catch (NumberFormatException nfe) {
                lblMsg.setText("Valor inválido! Digite um valor numérico (ex: 250.00).");
                lblMsg.setStyle("-fx-text-fill: red;");
            } catch (Exception ex) { 
                lblMsg.setText("Erro: " + ex.getMessage()); 
                lblMsg.setStyle("-fx-text-fill: red;");
            }
        });

        btnLimpar.setOnAction(e -> {
            txtId.clear(); txtValor.clear(); cbCarros.setValue(null); cbFuncs.setValue(null); cbContas.setValue(null);
            lblMsg.setText("");
        });

        btnExcluir.setOnAction(e -> {
            Pagamento p = tabela.getSelectionModel().getSelectedItem();
            if (p != null) {
                try {
                    Conta c = p.getConta();
                    if (c != null) {
                        c.setSaldo(c.getSaldo().subtract(p.getValor()));
                        contaDao.atualizar(c);
                    }
                    pagamentoDao.remover(p.getId());
                    lblMsg.setText("Pagamento excluído e saldo da conta estornado!");
                    lblMsg.setStyle("-fx-text-fill: green;");
                    txtId.clear(); txtValor.clear(); cbCarros.setValue(null); cbFuncs.setValue(null); cbContas.setValue(null);
                    recarregarListas.run();
                } catch (Exception ex) {
                    lblMsg.setText("Erro ao excluir: " + ex.getMessage());
                    lblMsg.setStyle("-fx-text-fill: red;");
                }
            } else {
                lblMsg.setText("Selecione um pagamento para excluir!");
                lblMsg.setStyle("-fx-text-fill: red;");
            }
        });

        VBox vbox = new VBox(15, grid, tabela);
        vbox.setPadding(new Insets(15));
        tab.setContent(vbox);
        return tab;
    }

    // --- ABA CONTAS ---
    private Tab criarAbaContas() {
        Tab tab = new Tab("Contas & Saldo");
        tab.setClosable(false);

        // --- PAINEL ESQUERDO: GESTÃO DE CONTAS ---
        GridPane gridConta = criarGrid();
        TextField txtIdConta = new TextField();
        txtIdConta.setVisible(false);
        TextField txtNomeConta = new TextField();
        txtNomeConta.setPromptText("Ex: Caixa da Oficina, Banco Inter");
        TextField txtSaldoInicial = new TextField();
        txtSaldoInicial.setPromptText("Ex: 1000.00");

        Button btnSalvarConta = new Button("Salvar / Atualizar Conta");
        Button btnLimparConta = new Button("Limpar");
        Button btnExcluirConta = new Button("Excluir Conta");
        Label lblMsgConta = new Label();

        TableView<Conta> tabelaContas = new TableView<>();
        tabelaContas.setPrefHeight(280);
        tabelaContas.setPrefWidth(430);

        TableColumn<Conta, Long> colContaId = new TableColumn<>("ID");
        colContaId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colContaId.setPrefWidth(45);

        TableColumn<Conta, String> colContaNome = new TableColumn<>("Nome da Conta");
        colContaNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colContaNome.setPrefWidth(240);

        TableColumn<Conta, BigDecimal> colContaSaldo = new TableColumn<>("Saldo Atual (R$)");
        colContaSaldo.setCellValueFactory(new PropertyValueFactory<>("saldo"));
        colContaSaldo.setCellFactory(column -> new TableCell<Conta, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal saldo, boolean empty) {
                super.updateItem(saldo, empty);
                if (empty || saldo == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(String.format("R$ %.2f", saldo));
                    if (saldo.compareTo(BigDecimal.ZERO) < 0) {
                        setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #1b5e20; -fx-font-weight: bold;");
                    }
                }
            }
        });
        colContaSaldo.setPrefWidth(140);

        tabelaContas.getColumns().addAll(colContaId, colContaNome, colContaSaldo);

        gridConta.add(new Label("Nome da Conta:"), 0, 0); gridConta.add(txtNomeConta, 1, 0);
        gridConta.add(new Label("Saldo Inicial (R$):"), 0, 1); gridConta.add(txtSaldoInicial, 1, 1);
        gridConta.add(new HBox(8, btnSalvarConta, btnLimparConta, btnExcluirConta), 1, 2);
        gridConta.add(lblMsgConta, 1, 3);

        Label lblTituloContas = new Label("🏦 Gerenciamento de Contas e Saldos");
        lblTituloContas.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        VBox painelEsquerdo = new VBox(10, lblTituloContas, gridConta, tabelaContas);
        painelEsquerdo.setPrefWidth(440);

        // --- PAINEL DIREITO: REGISTRO DE SAÍDA DE DINHEIRO ---
        GridPane gridSaida = criarGrid();
        ComboBox<Conta> cbContaSaida = new ComboBox<>();
        cbContaSaida.setPromptText("Selecione a conta de saída");

        cbContaSaida.setCellFactory(p -> new ListCell<Conta>() {
            @Override
            protected void updateItem(Conta c, boolean empty) {
                super.updateItem(c, empty);
                setText(empty || c == null ? null : c.getNome() + " [Saldo: R$ " + c.getSaldo() + "]");
            }
        });
        cbContaSaida.setConverter(new StringConverter<Conta>() {
            @Override
            public String toString(Conta c) {
                return c == null ? "" : c.getNome() + " [Saldo: R$ " + c.getSaldo() + "]";
            }
            @Override
            public Conta fromString(String string) { return null; }
        });

        TextField txtValorSaida = new TextField();
        txtValorSaida.setPromptText("Ex: 150.00");
        TextField txtMotivoSaida = new TextField();
        txtMotivoSaida.setPromptText("Ex: Compra de Peças, Almoço, Aluguel");

        Button btnRegistrarSaida = new Button("Registrar Saída (Debitar)");
        Button btnExcluirSaida = new Button("Estornar Saída Selecionada");
        Label lblMsgSaida = new Label();

        TableView<Saida> tabelaSaidas = new TableView<>();
        tabelaSaidas.setPrefHeight(280);
        tabelaSaidas.setPrefWidth(460);

        TableColumn<Saida, Long> colSaidaId = new TableColumn<>("ID");
        colSaidaId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colSaidaId.setPrefWidth(40);

        TableColumn<Saida, Conta> colSaidaConta = new TableColumn<>("Conta");
        colSaidaConta.setCellValueFactory(new PropertyValueFactory<>("conta"));
        colSaidaConta.setCellFactory(c -> new TableCell<Saida, Conta>() {
            @Override
            protected void updateItem(Conta conta, boolean empty) {
                super.updateItem(conta, empty);
                setText(empty || conta == null ? "-" : conta.getNome());
            }
        });
        colSaidaConta.setPrefWidth(120);

        TableColumn<Saida, String> colSaidaMotivo = new TableColumn<>("Motivo da Saída");
        colSaidaMotivo.setCellValueFactory(new PropertyValueFactory<>("motivo"));
        colSaidaMotivo.setPrefWidth(150);

        TableColumn<Saida, BigDecimal> colSaidaValor = new TableColumn<>("Valor (R$)");
        colSaidaValor.setCellValueFactory(new PropertyValueFactory<>("valor"));
        colSaidaValor.setCellFactory(column -> new TableCell<Saida, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal val, boolean empty) {
                super.updateItem(val, empty);
                if (empty || val == null) {
                    setText(null);
                } else {
                    setText(String.format("- R$ %.2f", val));
                    setStyle("-fx-text-fill: #b71c1c; -fx-font-weight: bold;");
                }
            }
        });
        colSaidaValor.setPrefWidth(95);

        TableColumn<Saida, Date> colSaidaData = new TableColumn<>("Data");
        colSaidaData.setCellValueFactory(new PropertyValueFactory<>("dataSaida"));
        colSaidaData.setCellFactory(col -> new TableCell<Saida, Date>() {
            private final SimpleDateFormat formato = new SimpleDateFormat("dd/MM HH:mm");
            @Override
            protected void updateItem(Date data, boolean empty) {
                super.updateItem(data, empty);
                setText(empty || data == null ? null : formato.format(data));
            }
        });
        colSaidaData.setPrefWidth(75);

        tabelaSaidas.getColumns().addAll(colSaidaId, colSaidaConta, colSaidaMotivo, colSaidaValor, colSaidaData);

        gridSaida.add(new Label("Conta de Saída:"), 0, 0); gridSaida.add(cbContaSaida, 1, 0);
        gridSaida.add(new Label("Valor da Saída (R$):"), 0, 1); gridSaida.add(txtValorSaida, 1, 1);
        gridSaida.add(new Label("Motivo / Despesa:"), 0, 2); gridSaida.add(txtMotivoSaida, 1, 2);
        gridSaida.add(new HBox(8, btnRegistrarSaida, btnExcluirSaida), 1, 3);
        gridSaida.add(lblMsgSaida, 1, 4);

        Label lblTituloSaidas = new Label("💸 Registrar Saída de Dinheiro (Despesas / Retiradas)");
        lblTituloSaidas.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        VBox painelDireito = new VBox(10, lblTituloSaidas, gridSaida, tabelaSaidas);
        painelDireito.setPrefWidth(470);

        // --- FUNÇÃO PARA ATUALIZAR AS TABELAS E COMBOS ---
        Runnable atualizarContasESaidas = () -> {
            try {
                var contas = contaDao.listarTodos();
                tabelaContas.setItems(FXCollections.observableArrayList(contas));
                cbContaSaida.setItems(FXCollections.observableArrayList(contas));
                tabelaSaidas.setItems(FXCollections.observableArrayList(saidaDao.listarTodos()));
            } catch (Exception ex) {
                lblMsgConta.setText("Aviso ao carregar: " + ex.getMessage());
                lblMsgConta.setStyle("-fx-text-fill: red;");
            }
        };

        tab.setOnSelectionChanged(e -> {
            if (tab.isSelected()) {
                atualizarContasESaidas.run();
            }
        });

        tabelaContas.setOnMouseClicked(e -> {
            Conta c = tabelaContas.getSelectionModel().getSelectedItem();
            if (c != null) {
                txtIdConta.setText(c.getId().toString());
                txtNomeConta.setText(c.getNome());
                txtSaldoInicial.setText(c.getSaldo() != null ? c.getSaldo().toString() : "0.00");
                cbContaSaida.setValue(c);
            }
        });

        // --- EVENTOS DO PAINEL DE CONTAS ---
        btnSalvarConta.setOnAction(e -> {
            try {
                if (txtNomeConta.getText().trim().isEmpty() || txtSaldoInicial.getText().trim().isEmpty()) {
                    lblMsgConta.setText("Preencha o nome da conta e o saldo!");
                    lblMsgConta.setStyle("-fx-text-fill: red;");
                    return;
                }

                BigDecimal saldo = new BigDecimal(txtSaldoInicial.getText().trim().replace(",", "."));
                if (txtIdConta.getText().isEmpty()) {
                    Conta nova = new Conta(txtNomeConta.getText().trim(), saldo);
                    contaDao.salvar(nova);
                    lblMsgConta.setText("Conta cadastrada com sucesso!");
                } else {
                    Long id = Long.parseLong(txtIdConta.getText());
                    Conta c = contaDao.listarTodos().stream().filter(x -> x.getId().equals(id)).findFirst().orElse(null);
                    if (c != null) {
                        c.setNome(txtNomeConta.getText().trim());
                        c.setSaldo(saldo);
                        contaDao.atualizar(c);
                        lblMsgConta.setText("Conta atualizada com sucesso!");
                    }
                }

                lblMsgConta.setStyle("-fx-text-fill: green;");
                txtIdConta.clear(); txtNomeConta.clear(); txtSaldoInicial.clear();
                atualizarContasESaidas.run();
            } catch (NumberFormatException nfe) {
                lblMsgConta.setText("Saldo inválido! Use formato ex: 1000.00");
                lblMsgConta.setStyle("-fx-text-fill: red;");
            } catch (Exception ex) {
                lblMsgConta.setText("Erro: " + ex.getMessage());
                lblMsgConta.setStyle("-fx-text-fill: red;");
            }
        });

        btnLimparConta.setOnAction(e -> {
            txtIdConta.clear(); txtNomeConta.clear(); txtSaldoInicial.clear();
            lblMsgConta.setText("");
        });

        btnExcluirConta.setOnAction(e -> {
            Conta c = tabelaContas.getSelectionModel().getSelectedItem();
            if (c != null) {
                try {
                    contaDao.remover(c.getId());
                    atualizarContasESaidas.run();
                    txtIdConta.clear(); txtNomeConta.clear(); txtSaldoInicial.clear();
                    lblMsgConta.setText("Conta excluída com sucesso!");
                    lblMsgConta.setStyle("-fx-text-fill: green;");
                } catch (Exception ex) {
                    lblMsgConta.setText("Não é possível excluir conta com pagamentos ou saídas vinculados.");
                    lblMsgConta.setStyle("-fx-text-fill: red;");
                }
            } else {
                lblMsgConta.setText("Selecione uma conta na tabela para excluir!");
                lblMsgConta.setStyle("-fx-text-fill: red;");
            }
        });

        // --- EVENTOS DO PAINEL DE SAÍDAS DE DINHEIRO ---
        btnRegistrarSaida.setOnAction(e -> {
            try {
                Conta contaOrigem = cbContaSaida.getValue();
                if (contaOrigem == null || txtValorSaida.getText().trim().isEmpty() || txtMotivoSaida.getText().trim().isEmpty()) {
                    lblMsgSaida.setText("Selecione a conta, valor e motivo da saída!");
                    lblMsgSaida.setStyle("-fx-text-fill: red;");
                    return;
                }

                BigDecimal valor = new BigDecimal(txtValorSaida.getText().trim().replace(",", "."));
                if (valor.compareTo(BigDecimal.ZERO) <= 0) {
                    lblMsgSaida.setText("O valor da saída deve ser maior que zero!");
                    lblMsgSaida.setStyle("-fx-text-fill: red;");
                    return;
                }

                // Cria o registro da saída
                Saida saida = new Saida(txtMotivoSaida.getText().trim(), valor, contaOrigem);
                saidaDao.salvar(saida);

                // Debita o saldo da conta
                contaOrigem.setSaldo(contaOrigem.getSaldo().subtract(valor));
                contaDao.atualizar(contaOrigem);

                lblMsgSaida.setText("Saída de R$ " + valor + " registrada com sucesso na conta " + contaOrigem.getNome() + "!");
                lblMsgSaida.setStyle("-fx-text-fill: green;");

                txtValorSaida.clear();
                txtMotivoSaida.clear();
                cbContaSaida.setValue(null);
                atualizarContasESaidas.run();
            } catch (NumberFormatException nfe) {
                lblMsgSaida.setText("Valor inválido! Digite um valor numérico (ex: 150.00).");
                lblMsgSaida.setStyle("-fx-text-fill: red;");
            } catch (Exception ex) {
                lblMsgSaida.setText("Erro: " + ex.getMessage());
                lblMsgSaida.setStyle("-fx-text-fill: red;");
            }
        });

        btnExcluirSaida.setOnAction(e -> {
            Saida saida = tabelaSaidas.getSelectionModel().getSelectedItem();
            if (saida != null) {
                try {
                    // Ao estornar a saída, devolve o dinheiro para o saldo da conta
                    Conta c = saida.getConta();
                    if (c != null) {
                        c.setSaldo(c.getSaldo().add(saida.getValor()));
                        contaDao.atualizar(c);
                    }
                    saidaDao.remover(saida.getId());
                    atualizarContasESaidas.run();
                    lblMsgSaida.setText("Saída estornada e saldo devolvido à conta!");
                    lblMsgSaida.setStyle("-fx-text-fill: green;");
                } catch (Exception ex) {
                    lblMsgSaida.setText("Erro ao estornar: " + ex.getMessage());
                    lblMsgSaida.setStyle("-fx-text-fill: red;");
                }
            } else {
                lblMsgSaida.setText("Selecione uma saída na tabela para estornar!");
                lblMsgSaida.setStyle("-fx-text-fill: red;");
            }
        });

        HBox painelGeral = new HBox(20, painelEsquerdo, painelDireito);
        painelGeral.setPadding(new Insets(15));
        tab.setContent(painelGeral);
        return tab;
    }

    private GridPane criarGrid() {
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10));
        grid.setVgap(10);
        grid.setHgap(10);
        return grid;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
