import java.io.*;
import java.util.*;

public class Projecto2B {

    public static final String ARQUIVO_CLIENTES = "Clientes.txt";
    public static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        while (true) {
            System.out.println("\n--- MENU ---");
            System.out.println("1. Inserir registo");
            System.out.println("2. Consultar cliente");
            System.out.println("3. Gerar fatura");
            System.out.println("4. Eliminar registos");
            System.out.println("5. Sair");
            System.out.print("Escolha: ");
            String opcao = scanner.nextLine();

            switch (opcao) {
                case "1" -> inserirRegisto();
                case "2" -> consultarCliente();
                case "3" -> gerarFatura();
                case "4" -> eliminarRegistos();
                case "5" -> System.exit(0);
                default -> System.out.println("Opção inválida.");
            }
        }
    }

    private static void inserirRegisto() {
        System.out.print("Numero Cliente: ");
        String cliente = scanner.nextLine();
        System.out.print("Numero Destino: ");
        String destino = scanner.nextLine();
        System.out.print("Tempo (s): ");
        String tempo = scanner.nextLine();

        if (cliente.isEmpty() || destino.isEmpty() || tempo.isEmpty()) {
            System.out.println("Todos os campos são obrigatórios.");
            return;
        }

        try (PrintWriter out = new PrintWriter(new FileWriter(ARQUIVO_CLIENTES, true))) {
            out.println(cliente + "," + destino + "," + tempo);
            System.out.println("Registo inserido com sucesso.");
        } catch (IOException e) {
            System.out.println("Erro ao escrever no ficheiro: " + e.getMessage());
        }
    }

    private static void consultarCliente() {
        System.out.print("Numero do cliente a consultar: ");
        String nome = scanner.nextLine();

        try (BufferedReader br = new BufferedReader(new FileReader(ARQUIVO_CLIENTES))) {
            String linha;
            boolean encontrou = false;
            while ((linha = br.readLine()) != null) {
                String[] partes = linha.split(",");
                if (partes.length >= 3 && partes[0].equalsIgnoreCase(nome)) {
                    String regiao = detectarRegiao(partes[1]);
                    String valor = calcularValorTotal(partes[1], partes[2]);
                    System.out.printf("Cliente: %s | Destino: %s | Tempo: %s | Região: %s | Valor: %s%n",
                            partes[0], partes[1], partes[2], regiao, valor);
                    encontrou = true;
                }
            }
            if (!encontrou) {
                System.out.println("Nenhum registo encontrado.");
            }
        } catch (IOException e) {
            System.out.println("Erro ao ler o ficheiro: " + e.getMessage());
        }
    }

    private static void gerarFatura() {
        System.out.print("Nome do cliente: ");
        String nome = scanner.nextLine();
        File fatura = new File("Factura_" + nome + ".txt");

        try (
            BufferedReader br = new BufferedReader(new FileReader(ARQUIVO_CLIENTES));
            PrintWriter pw = new PrintWriter(new FileWriter(fatura))
        ) {
            pw.println("FACTURA");
            pw.println("EMPRESA DE COMUNICACAO US\n");
            pw.println("CLIENTE: " + nome + "\n");
            pw.printf("%-20s %-10s %-15s %-10s%n", "DESTINO", "TEMPO", "REGIÃO", "VALOR");

            double total = 0;
            String linha;
            while ((linha = br.readLine()) != null) {
                String[] partes = linha.split(",");
                if (partes.length >= 3 && partes[0].equalsIgnoreCase(nome)) {
                    String regiao = detectarRegiao(partes[1]);
                    String valorStr = calcularValorTotal(partes[1], partes[2]);
                    double valor = Double.parseDouble(valorStr.replace("ECV", "").trim().replace(",", "."));
                    pw.printf("%-20s %-10s %-15s %-10.2f%n", partes[1], partes[2], regiao, valor);
                    total += valor;
                }
            }

            pw.printf("%nTOTAL PAGO: %.2f ECV%n", total);
            System.out.println("Fatura gerada: " + fatura.getAbsolutePath());

        } catch (IOException e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    private static void eliminarRegistos() {
        System.out.print("Nome do cliente: ");
        String nome = scanner.nextLine();
        System.out.print("Eliminar todos os registos do cliente? (s/n): ");
        String todos = scanner.nextLine().trim().toLowerCase();

        File inputFile = new File(ARQUIVO_CLIENTES);
        File tempFile = new File("temp.csv");

        try (
            BufferedReader reader = new BufferedReader(new FileReader(inputFile));
            PrintWriter writer = new PrintWriter(new FileWriter(tempFile))
        ) {
            String linha;
            while ((linha = reader.readLine()) != null) {
                String[] partes = linha.split(",");
                if (partes.length >= 3) {
                    if (todos.equals("s")) {
                        if (!partes[0].equalsIgnoreCase(nome)) {
                            writer.println(linha);
                        }
                    } else {
                        System.out.printf("Eliminar este registo? %s (s/n): ", linha);
                        String resposta = scanner.nextLine().trim().toLowerCase();
                        if (!resposta.equals("s")) {
                            writer.println(linha);
                        }
                    }
                }
            }

            if (inputFile.delete() && tempFile.renameTo(inputFile)) {
                System.out.println("Registos eliminados com sucesso.");
            } else {
                System.out.println("Erro ao atualizar o ficheiro.");
            }
        } catch (IOException e) {
            System.out.println("Erro ao processar ficheiros: " + e.getMessage());
        }
    }

    private static String detectarRegiao(String destino) {
        if (destino.startsWith("+") || destino.startsWith("00")) return "Internacional";
        if (destino.startsWith("2") || destino.startsWith("3") || destino.startsWith("5") || destino.startsWith("9")) return "Nacional";
        return "Desconhecida";
    }

    private static String calcularValorTotal(String destino, String tempoStr) {
        try {
            double tempo = Double.parseDouble(tempoStr);
            double precoPorSegundo;

            if (destino.startsWith("+") || destino.startsWith("00")) {
                precoPorSegundo = 1.05;
            } else if (destino.startsWith("2") || destino.startsWith("3")) {
                precoPorSegundo = 0.24;
            } else if (destino.startsWith("8") || destino.startsWith("9")) {
                precoPorSegundo = 0.43;
            } else {
                return "Indefinido";
            }

            double total = tempo * precoPorSegundo;
            return String.format("%.2f ECV", total);
        } catch (NumberFormatException e) {
            return "Erro";
        }
    }
}
