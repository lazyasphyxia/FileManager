import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Класс для отображения содержимого директории.
 * <p>
 * Предоставляет методы для вывода списка файлов и папок с указанием их типа и размера.
 * </p>
 */
public class DirectoryDisplay {

    /**
     * Конструктор класса DirectoryDisplay.
     * Создаёт новый экземпляр для отображения содержимого директорий.
     */
    public DirectoryDisplay() {
        // Конструктор по умолчанию
    }

    /**
     * Отображает содержимое текущей директории с размерами файлов.
     *
     * @param directoryPath путь к директории, содержимое которой нужно отобразить
     */
    public void displayDirectoryContents(String directoryPath) {
        Path path = Paths.get(directoryPath).normalize();

        if (!Files.exists(path) || !Files.isDirectory(path)) {
            System.out.println("Директория не существует или недоступна: " + directoryPath);
            return;
        }

        System.out.println("\n=== Содержимое директории: " + path.toAbsolutePath() + " ===");
        System.out.printf("%-30s %-15s %-20s%n", "Имя файла/папки", "Тип", "Размер");
        System.out.println("--------------------------------------------------------------");

        try {
            Files.list(path).forEach(file -> {
                try {
                    String name = file.getFileName().toString();
                    String type = Files.isDirectory(file) ? "Папка" : "Файл";
                    String size = FileSizeFormatter.formatFileSize(Files.size(file));
                    System.out.printf("%-30s %-15s %-20s%n", name, type, size);
                } catch (IOException e) {
                    System.err.println("Ошибка при получении информации о файле " + file.getFileName() + ": " + e.getMessage());
                }
            });
        } catch (IOException e) {
            System.err.println("Ошибка при чтении директории: " + e.getMessage());
        }
    }
}