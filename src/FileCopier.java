import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Scanner;

/**
 * Класс для выполнения операций копирования файлов.
 * <p>
 * Отвечает за обработку команды 'copy', проверку исходного файла,
 * создание целевой директории и непосредственное копирование файла.
 * </p>
 */
public class FileCopier {

    /**
     * Конструктор класса FileCopier.
     * Создаёт новый экземпляр для выполнения операций копирования файлов.
     */
    public FileCopier() {
        // Конструктор по умолчанию
    }

    /**
     * Обрабатывает команду копирования файла.
     *
     * @param command          полная строка команды, начинающаяся с 'copy'
     * @param currentDirectory текущая директория, из которой копируется файл
     */
    public void handleCopyCommand(String command, String currentDirectory, Scanner scanner) {
        // Находим индекс последнего пробела в строке (перед целевой директорией)
        int lastSpaceIndex = command.lastIndexOf(' ');
        if (lastSpaceIndex == -1 || lastSpaceIndex <= 4) { // "copy" + пробел = 5 символов минимум
            System.out.println("Неверный формат команды. Используйте: copy <имя_файла> <целевая_директория>");
            return;
        }

        String sourceFileName = command.substring(5, lastSpaceIndex).trim(); // после "copy "
        String targetDirectoryPath = command.substring(lastSpaceIndex + 1).trim();

        Path sourcePath = Paths.get(sourceFileName).normalize();
        // Вот здесь исправление:
        Path targetDirectory = Paths.get(currentDirectory).resolve(targetDirectoryPath).normalize();

        try {
            // Если путь к файлу не абсолютный, строим относительно текущей директории
            if (!sourcePath.isAbsolute()) {
                sourcePath = Paths.get(currentDirectory, sourceFileName).normalize();
            }

            validateSourceFile(sourcePath);
            ensureTargetDirectoryExists(targetDirectory); // <-- Без проверки рекурсии
            Path targetPath = copyFile(sourcePath, targetDirectory);
            System.out.println("Файл успешно скопирован: " + targetPath.toAbsolutePath());
        } catch (Exception e) {
            System.err.println("Ошибка при копировании файла: " + e.getMessage());
        }
    }

    /**
     * Проверяет существование и доступность исходного файла.
     *
     * @param sourcePath путь к исходному файлу
     * @throws IOException если файл не существует, не является файлом или недоступен для чтения
     */
    private void validateSourceFile(Path sourcePath) throws IOException {
        if (!Files.exists(sourcePath)) {
            throw new IOException("Исходный файл не существует: " + sourcePath);
        }
        if (Files.isDirectory(sourcePath)) {
            throw new IOException("Исходный путь является директорией, а не файлом: " + sourcePath);
        }
        if (!Files.isReadable(sourcePath)) {
            throw new IOException("Исходный файл недоступен для чтения: " + sourcePath);
        }
    }

    /**
     * Создает целевую директорию, если она не существует.
     *
     * @param targetDirectory путь к целевой директории
     * @throws IOException если директория не может быть создана или путь существует, но не является директорией
     */
    private void ensureTargetDirectoryExists(Path targetDirectory) throws IOException {
        if (!Files.exists(targetDirectory)) {
            Files.createDirectories(targetDirectory);
            System.out.println("Создана целевая директория: " + targetDirectory.toAbsolutePath());
        } else if (!Files.isDirectory(targetDirectory)) {
            throw new IOException("Целевой путь не является директорией: " + targetDirectory);
        }
    }

    /**
     * Копирует файл в указанную директорию.
     * Если файл с таким именем уже существует, генерируется новое имя с суффиксом.
     *
     * @param sourcePath      путь к исходному файлу
     * @param targetDirectory путь к целевой директории
     * @return путь к скопированному файлу
     * @throws IOException если возникла ошибка при копировании
     */
    private Path copyFile(Path sourcePath, Path targetDirectory) throws IOException {
        String fileName = sourcePath.getFileName().toString();
        Path targetPath = targetDirectory.resolve(fileName);

        // Если файл с таким именем уже существует, добавляем суффикс
        int counter = 1;
        while (Files.exists(targetPath)) {
            String baseName = fileName.substring(0, fileName.lastIndexOf('.'));
            String extension = fileName.substring(fileName.lastIndexOf('.'));
            targetPath = targetDirectory.resolve(baseName + "_" + counter + extension);
            counter++;
        }

        Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
        return targetPath;
    }
}