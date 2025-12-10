import java.util.Scanner;

/**
 * Основной класс запуска и управления файловым менеджером.
 * <p>
 * Обеспечивает навигацию по файловой системе, копирование файлов и отображение содержимого директорий.
 * </p>
 */
public class FileManager {

    private final DirectoryDisplay directoryDisplay;
    private final FileCopier fileCopier;
    private final Scanner scanner;

    /**
     * Конструктор класса FileManager. Инициализирует зависимости.
     */
    public FileManager() {
        this.directoryDisplay = new DirectoryDisplay();
        this.fileCopier = new FileCopier();
        this.scanner = new Scanner(System.in);
    }

    /**
     * Запуск основного цикла работы файлового менеджера.
     * Отображает содержимое текущей директории и обрабатывает команды пользователя.
     */
    public void start() {
        String currentDirectory = System.getProperty("user.dir");

        while (true) {
            try {
                directoryDisplay.displayDirectoryContents(currentDirectory);
                String command = getCommand();

                if ("exit".equalsIgnoreCase(command)) {
                    break;
                }

                if (command.startsWith("copy ")) {
                    fileCopier.handleCopyCommand(command, currentDirectory, scanner);
                } else if (command.startsWith("cd ")) {
                    currentDirectory = handleChangeDirectoryCommand(command, currentDirectory);
                } else {
                    System.out.println("Неизвестная команда. Доступны команды: 'copy <имя_файла> <целевая_директория>', 'cd <путь>', 'exit'");
                }
            } catch (Exception e) {
                System.err.println("Ошибка выполнения команды: " + e.getMessage());
            }
        }

        scanner.close();
        System.out.println("Программа завершена.");
    }

    /**
     * Получает команду от пользователя из стандартного ввода.
     *
     * @return введённая пользователем команда, обрезанная от лишних пробелов
     */
    private String getCommand() {
        System.out.print("\nВведите команду (copy, cd или exit): ");
        return scanner.nextLine().trim();
    }

    /**
     * Обрабатывает команду смены директории.
     *
     * @param command          строка команды, начинающаяся с 'cd'
     * @param currentDirectory текущая директория, из которой производится переход
     * @return новая директория, если переход успешен, иначе — прежняя
     */
    private String handleChangeDirectoryCommand(String command, String currentDirectory) {
        String[] parts = command.split("\\s+", 2);
        if (parts.length < 2) {
            System.out.println("Неверный формат команды. Используйте: cd <путь>");
            return currentDirectory;
        }

        String targetPath = parts[1].trim(); // Обрезаем лишние пробелы

        java.nio.file.Path newPath = java.nio.file.Paths.get(currentDirectory).resolve(targetPath).normalize();

        // Проверка: не выходим за пределы корня диска
        if (isOutsideRoot(newPath, currentDirectory)) {
            System.out.println("Нельзя перейти выше корневой директории.");
            return currentDirectory;
        }

        if (!java.nio.file.Files.exists(newPath) || !java.nio.file.Files.isDirectory(newPath)) {
            System.out.println("Директория не существует или не является папкой: " + newPath);
            return currentDirectory;
        }

        if (!java.nio.file.Files.isReadable(newPath)) {
            System.out.println("Нет доступа к директории: " + newPath);
            return currentDirectory;
        }

        System.out.println("Перешли в директорию: " + newPath.toAbsolutePath());
        return newPath.toString();
    }

    /**
     * Проверяет, выходит ли путь за пределы корневой директории.
     *
     * @param newPath          новый путь
     * @param currentDirectory текущая директория
     * @return true, если путь находится выше корня
     */
    private boolean isOutsideRoot(java.nio.file.Path newPath, String currentDirectory) {
        java.nio.file.Path currentRoot = java.nio.file.Paths.get(currentDirectory).getRoot();
        java.nio.file.Path newRoot = newPath.getRoot();

        // Если корень изменился — значит, мы вышли за пределы текущего диска
        if (newRoot != null && !newRoot.equals(currentRoot)) {
            return true;
        }

        return false;
    }

    /**
     * Точка входа в приложение. Создаёт и запускает экземпляр файлового менеджера.
     *
     * @param args аргументы командной строки (не используются)
     */
    public static void main(String[] args) {
        try {
            FileManager fileManager = new FileManager();
            fileManager.start();
        } catch (Exception e) {
            System.err.println("Критическая ошибка при запуске программы: " + e.getMessage());
            e.printStackTrace();
        }
    }
}