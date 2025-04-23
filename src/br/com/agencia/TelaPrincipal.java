package br.com.agencia;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

// Classe Usuario
class Usuario implements Serializable {
    private static final long serialVersionUID = 1L;
    private String login;
    private String senha;
    private String nome;

    public Usuario(String login, String senha, String nome) {
        this.login = login;
        this.senha = senha;
        this.nome = nome;
    }

    public String getLogin() { return login; }
    public String getSenha() { return senha; }
    public String getNome() { return nome; }

    public void setSenha(String novaSenha) {
        this.senha = novaSenha;
    }
}

// Classe Conta
class Conta implements Serializable {
    private static final long serialVersionUID = 1L;
    private int numero;
    private String titular;
    private double saldo;
    private List<Transacao> transacoes = new ArrayList<>();

    public Conta(int numero, String titular) {
        this.numero = numero;
        this.titular = titular;
        this.saldo = 0.0;
    }

    public int getNumero() { return numero; }
    public String getTitular() { return titular; }
    public double getSaldo() { return saldo; }
    public List<Transacao> getTransacoes() { return transacoes; }

    public void depositar(double valor) {
        if (valor > 0) {
            saldo += valor;
            transacoes.add(new Transacao("DEPÓSITO", valor, "Depósito em conta"));
        }
    }

    public boolean sacar(double valor) {
        if (valor > 0 && saldo >= valor) {
            saldo -= valor;
            transacoes.add(new Transacao("SAQUE", valor, "Saque em conta"));
            return true;
        }
        return false;
    }

    public boolean transferir(Conta destino, double valor) {
        if (this.sacar(valor)) {
            destino.depositar(valor);
            transacoes.add(new Transacao("TRANSFERÊNCIA", valor,
                    "Transferência para conta " + destino.getNumero() + " - " + destino.getTitular()));
            destino.transacoes.add(new Transacao("TRANSFERÊNCIA", valor,
                    "Transferência da conta " + this.numero + " - " + this.titular));
            return true;
        }
        return false;
    }

    public String getExtrato() {
        StringBuilder sb = new StringBuilder();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        sb.append("Extrato da Conta ").append(numero).append("\n");
        sb.append("Titular: ").append(titular).append("\n\n");
        sb.append("Data/Hora               Tipo          Valor       Descrição\n");
        sb.append("------------------------------------------------------------\n");

        for (Transacao t : transacoes) {
            sb.append(String.format("%-20s %-12s R$ %-10.2f %s\n",
                    sdf.format(t.getData()),
                    t.getTipo(),
                    t.getValor(),
                    t.getDescricao()));
        }

        sb.append("\nSaldo Atual: R$ ").append(String.format("%.2f", saldo));
        return sb.toString();
    }
}

// Classe Transacao
class Transacao implements Serializable {
    private static final long serialVersionUID = 1L;
    private Date data;
    private String tipo;
    private double valor;
    private String descricao;

    public Transacao(String tipo, double valor, String descricao) {
        this.data = new Date();
        this.tipo = tipo;
        this.valor = valor;
        this.descricao = descricao;
    }

    public Date getData() { return data; }
    public String getTipo() { return tipo; }
    public double getValor() { return valor; }
    public String getDescricao() { return descricao; }
}

// Classe Banco
class Banco implements Serializable {
    private static final long serialVersionUID = 1L;
    private List<Conta> contas = new ArrayList<>();
    private List<Usuario> usuarios = new ArrayList<>();
    private static final String ARQUIVO = "dados_banco.ser";

    public Banco() {
        carregar();
        if (usuarios.isEmpty()) {
            usuarios.add(new Usuario("admin", "1234", "Administrador"));
            usuarios.add(new Usuario("gerente", "ger123", "Gerente"));
            usuarios.add(new Usuario("cliente", "cli123", "Cliente"));
            salvar();
        }
    }

    public void adicionarConta(Conta conta) {
        contas.add(conta);
        salvar();
    }

    public Conta buscarConta(int numero) {
        for (Conta conta : contas) {
            if (conta.getNumero() == numero) return conta;
        }
        return null;
    }

    public List<Conta> listarContas() {
        return contas;
    }

    public Usuario buscarUsuario(String login) {
        for (Usuario usuario : usuarios) {
            if (usuario.getLogin().equals(login)) {
                return usuario;
            }
        }
        return null;
    }

    public boolean autenticarUsuario(String login, String senha) {
        Usuario usuario = buscarUsuario(login);
        return usuario != null && usuario.getSenha().equals(senha);
    }

    public boolean cadastrarUsuario(String login, String senha, String nome) {
        if (buscarUsuario(login) != null) {
            return false;
        }
        usuarios.add(new Usuario(login, senha, nome));
        salvar();
        return true;
    }

    public void alterarSenha(String login, String novaSenha) {
        Usuario usuario = buscarUsuario(login);
        if (usuario != null) {
            usuario.setSenha(novaSenha);
            salvar();
        }
    }

    public void salvar() {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(ARQUIVO))) {
            Object[] dados = new Object[]{contas, usuarios};
            out.writeObject(dados);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Erro ao salvar os dados: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    public void carregar() {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(ARQUIVO))) {
            Object[] dados = (Object[]) in.readObject();
            contas = (List<Conta>) dados[0];
            usuarios = (List<Usuario>) dados[1];
        } catch (Exception e) {
            contas = new ArrayList<>();
            usuarios = new ArrayList<>();
        }
    }
}

// Tela de Login
class TelaLogin extends JFrame {
    private Banco banco;

    public TelaLogin() {
        banco = new Banco();
        configurarJanela();
        criarPainelLogin();
    }

    private void configurarJanela() {
        setTitle("Login - Sistema Bancário");
        setSize(400, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
    }

    private void criarPainelLogin() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(new Color(240, 240, 240));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Logo
        JLabel lblLogo = new JLabel("CAIXA 24 HORAS", SwingConstants.CENTER);
        lblLogo.setFont(new Font("Arial", Font.BOLD, 20));
        lblLogo.setForeground(new Color(0, 47, 108));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(lblLogo, gbc);

        // Ícone padrão
        JLabel lblIcon = new JLabel(UIManager.getIcon("OptionPane.informationIcon"));
        gbc.gridy = 1;
        panel.add(lblIcon, gbc);

        // Campos de login
        gbc.gridwidth = 1;
        gbc.gridy = 2;
        gbc.gridx = 0;
        panel.add(new JLabel("Usuário:"), gbc);

        gbc.gridx = 1;
        JTextField txtUsuario = new JTextField(15);
        panel.add(txtUsuario, gbc);

        gbc.gridy = 3;
        gbc.gridx = 0;
        panel.add(new JLabel("Senha:"), gbc);

        gbc.gridx = 1;
        JPasswordField txtSenha = new JPasswordField(15);
        panel.add(txtSenha, gbc);

        // Link "Esqueci a senha"
        gbc.gridy = 4;
        gbc.gridx = 1;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.LINE_END;
        JLabel lblEsqueciSenha = new JLabel("<html><u>Esqueci a senha</u></html>");
        lblEsqueciSenha.setForeground(Color.BLUE);
        lblEsqueciSenha.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        lblEsqueciSenha.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                String usuario = JOptionPane.showInputDialog(TelaLogin.this,
                        "Digite seu nome de usuário para recuperar a senha:");

                Usuario user = banco.buscarUsuario(usuario);
                if (user != null) {
                    JOptionPane.showMessageDialog(TelaLogin.this,
                            "Sua senha é: " + user.getSenha() +
                                    "\nPor favor, altere após o login.",
                            "Recuperação de Senha", JOptionPane.INFORMATION_MESSAGE);
                } else if (usuario != null) {
                    JOptionPane.showMessageDialog(TelaLogin.this,
                            "Usuário não encontrado.", "Erro", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        panel.add(lblEsqueciSenha, gbc);

        // Botão de Login
        gbc.gridy = 5;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.CENTER;

        JButton btnLogin = new JButton("Login");
        btnLogin.setBackground(new Color(0, 47, 108));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFocusPainted(false);
        btnLogin.setIcon(UIManager.getIcon("OptionPane.questionIcon"));

        btnLogin.addActionListener(e -> {
            if (banco.autenticarUsuario(txtUsuario.getText(), new String(txtSenha.getPassword()))) {
                new TelaPrincipal(banco).setVisible(true);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Usuário ou senha inválidos!",
                        "Erro de Login", JOptionPane.ERROR_MESSAGE);
            }
        });

        panel.add(btnLogin, gbc);

        // Botão de Cadastro
        gbc.gridy = 6;
        JButton btnCadastrar = new JButton("Cadastrar novo usuário");
        btnCadastrar.setBackground(new Color(0, 100, 0));
        btnCadastrar.setForeground(Color.WHITE);
        btnCadastrar.setFocusPainted(false);
        btnCadastrar.setIcon(UIManager.getIcon("OptionPane.informationIcon"));

        btnCadastrar.addActionListener(e -> {
            abrirTelaCadastro();
        });

        panel.add(btnCadastrar, gbc);

        add(panel);
    }

    private void abrirTelaCadastro() {
        JDialog dialog = new JDialog(this, "Cadastro de Novo Usuário", true);
        dialog.setSize(350, 300);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new GridBagLayout());
        dialog.setResizable(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblTitulo = new JLabel("CADASTRO DE USUÁRIO", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 16));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        dialog.add(lblTitulo, gbc);

        gbc.gridwidth = 1;
        gbc.gridy = 1;
        gbc.gridx = 0;
        dialog.add(new JLabel("Nome completo:"), gbc);

        gbc.gridx = 1;
        JTextField txtNome = new JTextField(15);
        dialog.add(txtNome, gbc);

        gbc.gridy = 2;
        gbc.gridx = 0;
        dialog.add(new JLabel("Nome de usuário:"), gbc);

        gbc.gridx = 1;
        JTextField txtNovoUsuario = new JTextField(15);
        dialog.add(txtNovoUsuario, gbc);

        gbc.gridy = 3;
        gbc.gridx = 0;
        dialog.add(new JLabel("Senha:"), gbc);

        gbc.gridx = 1;
        JPasswordField txtNovaSenha = new JPasswordField(15);
        dialog.add(txtNovaSenha, gbc);

        gbc.gridy = 4;
        gbc.gridx = 0;
        dialog.add(new JLabel("Confirmar senha:"), gbc);

        gbc.gridx = 1;
        JPasswordField txtConfirmaSenha = new JPasswordField(15);
        dialog.add(txtConfirmaSenha, gbc);

        gbc.gridy = 5;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.CENTER;

        JButton btnConfirmar = new JButton("Confirmar Cadastro");
        btnConfirmar.setBackground(new Color(0, 100, 0));
        btnConfirmar.setForeground(Color.WHITE);
        btnConfirmar.addActionListener(e -> {
            String nome = txtNome.getText().trim();
            String usuario = txtNovoUsuario.getText().trim();
            String senha = new String(txtNovaSenha.getPassword());
            String confirmaSenha = new String(txtConfirmaSenha.getPassword());

            if (nome.isEmpty() || usuario.isEmpty() || senha.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Preencha todos os campos!", "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!senha.equals(confirmaSenha)) {
                JOptionPane.showMessageDialog(dialog, "As senhas não coincidem!", "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (banco.buscarUsuario(usuario) != null) {
                JOptionPane.showMessageDialog(dialog, "Nome de usuário já existe!", "Erro", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (banco.cadastrarUsuario(usuario, senha, nome)) {
                JOptionPane.showMessageDialog(dialog, "Cadastro realizado com sucesso!\nFaça login para continuar.",
                        "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
            } else {
                JOptionPane.showMessageDialog(dialog, "Erro ao cadastrar usuário!", "Erro", JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.add(btnConfirmar, gbc);

        dialog.setVisible(true);
    }
}

// Classe principal
public class TelaPrincipal extends JFrame {
    private Banco banco;
    private JPanel painelInformacoes;
    private boolean temaEscuro = false;
    private Color corFundoPadrao = new Color(0, 47, 108);
    private Color corFundoEscuro = new Color(20, 20, 40);
    private Color corTextoClaro = Color.BLACK;
    private Color corTextoEscuro = Color.WHITE;

    public TelaPrincipal(Banco banco) {
        this.banco = banco;
        configurarJanela();
        criarPainelPrincipal();
        setVisible(true);
    }

    private void configurarJanela() {
        setTitle("Caixa 24 Horas - Sistema Bancário");
        setSize(900, 650);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
    }

    private void criarPainelPrincipal() {
        JPanel painelPrincipal = new JPanel(new BorderLayout());
        painelPrincipal.setBackground(corFundoPadrao);
        painelPrincipal.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        painelPrincipal.add(criarCabecalho(), BorderLayout.NORTH);

        painelInformacoes = new JPanel();
        painelInformacoes.setBackground(Color.WHITE);
        painelInformacoes.setLayout(new BoxLayout(painelInformacoes, BoxLayout.Y_AXIS));
        painelInformacoes.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JScrollPane scrollPane = new JScrollPane(painelInformacoes);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(0, 47, 108), 2));
        painelPrincipal.add(scrollPane, BorderLayout.CENTER);

        painelPrincipal.add(criarPainelBotoes(), BorderLayout.SOUTH);

        setContentPane(painelPrincipal);
    }

    private JPanel criarCabecalho() {
        JPanel cabecalho = new JPanel(new BorderLayout());
        cabecalho.setBackground(temaEscuro ? corFundoEscuro : corFundoPadrao);
        cabecalho.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        JLabel lblLogo = new JLabel("CAIXA 24 HORAS", SwingConstants.CENTER);
        lblLogo.setFont(new Font("Arial", Font.BOLD, 24));
        lblLogo.setForeground(Color.WHITE);
        cabecalho.add(lblLogo, BorderLayout.CENTER);

        JLabel lblDataHora = new JLabel(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()));
        lblDataHora.setFont(new Font("Arial", Font.PLAIN, 14));
        lblDataHora.setForeground(Color.WHITE);
        cabecalho.add(lblDataHora, BorderLayout.EAST);

        new Timer(1000, e -> lblDataHora.setText(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()))).start();

        return cabecalho;
    }

    private JPanel criarPainelBotoes() {
        JPanel painelBotoes = new JPanel(new GridLayout(3, 4, 10, 10));
        painelBotoes.setBackground(temaEscuro ? corFundoEscuro : corFundoPadrao);
        painelBotoes.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        Font fonte = new Font("Arial", Font.BOLD, 14);
        Color corFundo = new Color(255, 204, 0);
        Color corEditar = new Color(0, 153, 51);
        Color corExcluir = new Color(204, 0, 0);
        Color corExtrato = new Color(100, 149, 237);
        Color corSenha = new Color(153, 102, 204);

        JButton btnCriar = criarBotaoComIcone("CRIAR CONTA", "add.png", fonte, corFundo);
        JButton btnDepositar = criarBotaoComIcone("DEPÓSITO", "deposit.png", fonte, corFundo);
        JButton btnSacar = criarBotaoComIcone("SAQUE", "withdraw.png", fonte, corFundo);
        JButton btnTransferir = criarBotaoComIcone("TRANSFERÊNCIA", "transfer.png", fonte, corFundo);
        JButton btnListar = criarBotaoComIcone("CONSULTAR", "list.png", fonte, corFundo);
        JButton btnExtrato = criarBotaoComIcone("EXTRATO", "statement.png", fonte, corExtrato);
        JButton btnEditar = criarBotaoComIcone("EDITAR CONTA", "edit.png", fonte, corEditar);
        JButton btnExcluir = criarBotaoComIcone("EXCLUIR CONTA", "delete.png", fonte, corExcluir);
        JButton btnAlterarSenha = criarBotaoComIcone("ALTERAR SENHA", "password.png", fonte, corSenha);
        JButton btnTema = criarBotaoComIcone(temaEscuro ? "TEMA CLARO" : "TEMA ESCURO", "theme.png", fonte, new Color(200, 200, 200));
        JButton btnSair = criarBotaoComIcone("SAIR", "exit.png", fonte, new Color(204, 0, 0));

        // Ações dos botões
        btnCriar.addActionListener(e -> {
            String nome = JOptionPane.showInputDialog(this, "Nome do titular:");
            if (nome != null && !nome.trim().isEmpty()) {
                int numero = banco.listarContas().size() + 1;
                banco.adicionarConta(new Conta(numero, nome));
                atualizarInformacoes("Conta criada com sucesso! Número: " + numero);
            }
        });

        btnDepositar.addActionListener(e -> {
            int numero = solicitarNumeroConta();
            Conta conta = banco.buscarConta(numero);
            if (conta != null) {
                double valor = solicitarValor("Informe o valor do depósito:");
                if (valor > 0) {
                    conta.depositar(valor);
                    banco.salvar();
                    atualizarInformacoes("Depósito de R$ " + String.format("%.2f", valor) + " realizado com sucesso na conta " + numero);
                } else {
                    atualizarInformacoes("Valor inválido para depósito!");
                }
            } else {
                atualizarInformacoes("Conta não encontrada!");
            }
        });

        btnSacar.addActionListener(e -> {
            int numero = solicitarNumeroConta();
            Conta conta = banco.buscarConta(numero);
            if (conta != null) {
                double valor = solicitarValor("Informe o valor do saque:");
                if (valor > 0) {
                    if (conta.sacar(valor)) {
                        banco.salvar();
                        atualizarInformacoes("Saque de R$ " + String.format("%.2f", valor) + " realizado com sucesso na conta " + numero);
                    } else {
                        atualizarInformacoes("Saldo insuficiente ou valor inválido!");
                    }
                } else {
                    atualizarInformacoes("Valor inválido para saque!");
                }
            } else {
                atualizarInformacoes("Conta não encontrada!");
            }
        });

        btnTransferir.addActionListener(e -> {
            int origem = solicitarNumeroConta("Conta de origem:");
            int destino = solicitarNumeroConta("Conta de destino:");

            if (origem == destino) {
                atualizarInformacoes("Não é possível transferir para a mesma conta!");
                return;
            }

            double valor = solicitarValor("Valor da transferência:");
            if (valor <= 0) {
                atualizarInformacoes("Valor inválido para transferência!");
                return;
            }

            Conta cOrigem = banco.buscarConta(origem);
            Conta cDestino = banco.buscarConta(destino);

            if (cOrigem != null && cDestino != null) {
                if (cOrigem.transferir(cDestino, valor)) {
                    banco.salvar();
                    atualizarInformacoes("Transferência de R$ " + String.format("%.2f", valor) +
                            " da conta " + origem + " (" + cOrigem.getTitular() + ") para a conta " +
                            destino + " (" + cDestino.getTitular() + ") realizada com sucesso!");
                } else {
                    atualizarInformacoes("Saldo insuficiente para transferência!");
                }
            } else {
                atualizarInformacoes("Conta(s) não encontrada(s)!");
            }
        });

        btnListar.addActionListener(e -> {
            painelInformacoes.removeAll();
            if (banco.listarContas().isEmpty()) {
                painelInformacoes.add(new JLabel("Nenhuma conta cadastrada no sistema."));
            } else {
                for (Conta c : banco.listarContas()) {
                    JLabel lblConta = new JLabel("Conta " + c.getNumero() + " - Titular: " + c.getTitular() +
                            " - Saldo: R$ " + String.format("%.2f", c.getSaldo()));
                    painelInformacoes.add(lblConta);
                }
            }
            painelInformacoes.revalidate();
            painelInformacoes.repaint();
        });

        btnExtrato.addActionListener(e -> {
            int numero = solicitarNumeroConta("Informe o número da conta para ver o extrato:");
            Conta conta = banco.buscarConta(numero);
            if (conta != null) {
                JTextArea textArea = new JTextArea(conta.getExtrato());
                textArea.setEditable(false);
                textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

                JScrollPane scrollPane = new JScrollPane(textArea);
                scrollPane.setPreferredSize(new Dimension(600, 400));

                JOptionPane.showMessageDialog(this, scrollPane, "Extrato da Conta " + numero,
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                atualizarInformacoes("Conta não encontrada!");
            }
        });

        btnEditar.addActionListener(e -> {
            int numero = solicitarNumeroConta("Informe o número da conta para editar:");
            Conta conta = banco.buscarConta(numero);
            if (conta != null) {
                String novoNome = JOptionPane.showInputDialog(this,
                        "Editar titular da conta " + numero + ":", conta.getTitular());
                if (novoNome != null && !novoNome.trim().isEmpty()) {
                    try {
                        java.lang.reflect.Field field = Conta.class.getDeclaredField("titular");
                        field.setAccessible(true);
                        field.set(conta, novoNome);
                        banco.salvar();
                        atualizarInformacoes("Conta " + numero + " editada com sucesso!");
                    } catch (Exception ex) {
                        atualizarInformacoes("Erro ao editar conta!");
                    }
                }
            } else {
                atualizarInformacoes("Conta não encontrada!");
            }
        });

        btnExcluir.addActionListener(e -> {
            int numero = solicitarNumeroConta("Informe o número da conta para excluir:");
            Conta conta = banco.buscarConta(numero);
            if (conta != null) {
                int confirmacao = JOptionPane.showConfirmDialog(this,
                        "Tem certeza que deseja excluir a conta " + numero + " de " + conta.getTitular() + "?",
                        "Confirmar exclusão", JOptionPane.YES_NO_OPTION);

                if (confirmacao == JOptionPane.YES_OPTION) {
                    try {
                        java.lang.reflect.Field field = Banco.class.getDeclaredField("contas");
                        field.setAccessible(true);
                        @SuppressWarnings("unchecked")
                        List<Conta> contas = (List<Conta>) field.get(banco);
                        contas.remove(conta);
                        banco.salvar();
                        atualizarInformacoes("Conta " + numero + " excluída com sucesso!");
                    } catch (Exception ex) {
                        atualizarInformacoes("Erro ao excluir conta!");
                    }
                }
            } else {
                atualizarInformacoes("Conta não encontrada!");
            }
        });

        btnAlterarSenha.addActionListener(e -> {
            String usuario = JOptionPane.showInputDialog(this, "Digite seu usuário:");
            if (usuario != null) {
                String senhaAtual = JOptionPane.showInputDialog(this, "Digite sua senha atual:");
                if (senhaAtual != null && banco.autenticarUsuario(usuario, senhaAtual)) {
                    String novaSenha = JOptionPane.showInputDialog(this, "Digite a nova senha:");
                    if (novaSenha != null && !novaSenha.trim().isEmpty()) {
                        banco.alterarSenha(usuario, novaSenha);
                        JOptionPane.showMessageDialog(this, "Senha alterada com sucesso!");
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Usuário ou senha inválidos!", "Erro", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        btnTema.addActionListener(e -> alternarTema());

        btnSair.addActionListener(e -> {
            int confirmacao = JOptionPane.showConfirmDialog(this,
                    "Tem certeza que deseja sair do sistema?", "Confirmar Saída", JOptionPane.YES_NO_OPTION);
            if (confirmacao == JOptionPane.YES_OPTION) {
                System.exit(0);
            }
        });

        painelBotoes.add(btnCriar);
        painelBotoes.add(btnDepositar);
        painelBotoes.add(btnSacar);
        painelBotoes.add(btnTransferir);
        painelBotoes.add(btnListar);
        painelBotoes.add(btnExtrato);
        painelBotoes.add(btnEditar);
        painelBotoes.add(btnExcluir);
        painelBotoes.add(btnAlterarSenha);
        painelBotoes.add(btnTema);
        painelBotoes.add(btnSair);

        return painelBotoes;
    }

    private JButton criarBotaoComIcone(String texto, String nomeIcone, Font fonte, Color cor) {
        JButton btn = new JButton(texto);

        // Mapeamento de ícones padrão do Java
        Icon icon = null;
        switch(nomeIcone) {
            case "add.png":
            case "add_user.png":
                icon = UIManager.getIcon("FileView.fileIcon");
                break;
            case "deposit.png":
                icon = UIManager.getIcon("FileView.upFolderIcon");
                break;
            case "withdraw.png":
                icon = UIManager.getIcon("FileView.directoryIcon");
                break;
            case "transfer.png":
                icon = UIManager.getIcon("FileView.hardDriveIcon");
                break;
            case "list.png":
                icon = UIManager.getIcon("Tree.leafIcon");
                break;
            case "statement.png":
            case "bank.png":
                icon = UIManager.getIcon("OptionPane.informationIcon");
                break;
            case "edit.png":
                icon = UIManager.getIcon("FileView.floppyDriveIcon");
                break;
            case "delete.png":
                icon = UIManager.getIcon("OptionPane.errorIcon");
                break;
            case "password.png":
            case "login.png":
                icon = UIManager.getIcon("OptionPane.questionIcon");
                break;
            case "theme.png":
                icon = UIManager.getIcon("OptionPane.warningIcon");
                break;
            case "exit.png":
                icon = UIManager.getIcon("InternalFrame.closeIcon");
                break;
        }

        if (icon != null) {
            btn.setIcon(icon);
        }

        btn.setFont(fonte);
        btn.setBackground(cor);
        btn.setForeground(Color.BLACK);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        btn.setHorizontalTextPosition(SwingConstants.RIGHT);
        btn.setVerticalTextPosition(SwingConstants.CENTER);

        return btn;
    }

    private void alternarTema() {
        temaEscuro = !temaEscuro;

        if (temaEscuro) {
            aplicarTemaEscuro();
        } else {
            aplicarTemaClaro();
        }
    }

    private void aplicarTemaEscuro() {
        getContentPane().setBackground(corFundoEscuro);
        painelInformacoes.setBackground(new Color(30, 30, 60));
        painelInformacoes.setForeground(corTextoEscuro);

        for (Component c : ((JPanel)getContentPane().getComponent(0)).getComponents()) {
            c.setBackground(corFundoEscuro);
            if (c instanceof JLabel) {
                ((JLabel)c).setForeground(corTextoEscuro);
            }
        }

        for (Component c : ((JPanel)getContentPane().getComponent(2)).getComponents()) {
            if (c instanceof JButton) {
                JButton btn = (JButton)c;
                if (btn.getText().equals("TEMA ESCURO") || btn.getText().equals("TEMA CLARO")) {
                    btn.setText("TEMA CLARO");
                    btn.setBackground(new Color(70, 70, 120));
                    btn.setForeground(corTextoEscuro);
                } else if (!btn.getText().equals("SAIR")) {
                    btn.setBackground(new Color(50, 50, 100));
                    btn.setForeground(corTextoEscuro);
                }
            }
        }

        ((JScrollPane)getContentPane().getComponent(1)).setBorder(
                BorderFactory.createLineBorder(new Color(60, 60, 90), 2));
    }

    private void aplicarTemaClaro() {
        getContentPane().setBackground(corFundoPadrao);
        painelInformacoes.setBackground(Color.WHITE);
        painelInformacoes.setForeground(corTextoClaro);

        for (Component c : ((JPanel)getContentPane().getComponent(0)).getComponents()) {
            c.setBackground(corFundoPadrao);
            if (c instanceof JLabel) {
                ((JLabel)c).setForeground(Color.WHITE);
            }
        }

        for (Component c : ((JPanel)getContentPane().getComponent(2)).getComponents()) {
            if (c instanceof JButton) {
                JButton btn = (JButton)c;
                if (btn.getText().equals("TEMA CLARO") || btn.getText().equals("TEMA ESCURO")) {
                    btn.setText("TEMA ESCURO");
                    btn.setBackground(new Color(255, 204, 0));
                    btn.setForeground(Color.BLACK);
                } else if (!btn.getText().equals("SAIR")) {
                    btn.setBackground(new Color(255, 204, 0));
                    btn.setForeground(Color.BLACK);
                }
            }
        }

        ((JScrollPane)getContentPane().getComponent(1)).setBorder(
                BorderFactory.createLineBorder(new Color(0, 47, 108), 2));
    }

    private int solicitarNumeroConta() {
        return solicitarNumeroConta("Informe o número da conta:");
    }

    private int solicitarNumeroConta(String mensagem) {
        String input = JOptionPane.showInputDialog(this, mensagem);
        try {
            return Integer.parseInt(input);
        } catch (Exception e) {
            return -1;
        }
    }

    private double solicitarValor(String mensagem) {
        String input = JOptionPane.showInputDialog(this, mensagem);
        try {
            return Double.parseDouble(input);
        } catch (Exception e) {
            return 0;
        }
    }

    private void atualizarInformacoes(String mensagem) {
        painelInformacoes.removeAll();
        JLabel lblMensagem = new JLabel(mensagem);
        lblMensagem.setFont(new Font("Arial", Font.PLAIN, 16));
        painelInformacoes.add(lblMensagem);
        painelInformacoes.revalidate();
        painelInformacoes.repaint();
    }

    public static void main(String[] args) {
        try {
            // Define o look and feel do sistema para melhor aparência
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            new TelaLogin().setVisible(true);
        });
    }
}