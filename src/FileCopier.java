import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
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
     * @param scanner          объект Scanner для потенциального запроса подтверждения (пока не используется напрямую)
     */
    public void handleCopyCommand(String command, String currentDirectory, Scanner scanner) {
        // Разбор аргументов команды с учётом кавычек
        List<String> arguments = parseArguments(command);
        if (arguments.size() < 3) { // "copy" + source + target
            System.out.println("Неверный формат команды. Используйте: copy <имя_файла> <целевая_директория> или copy \"<имя файла>\" \"<целевая директория>\"");
            return;
        }

        // Первый аргумент после 'copy' - это источник
        String sourceFileName = arguments.get(1);
        // Второй аргумент - это цель
        String targetDirectoryPath = arguments.get(2);

        Path sourcePath = Paths.get(sourceFileName).normalize();
        Path targetDirectory = Paths.get(currentDirectory).resolve(targetDirectoryPath).normalize();

        try {
            // Если путь к файлу не абсолютный, строим относительно текущей директории
            if (!sourcePath.isAbsolute()) {
                sourcePath = Paths.get(currentDirectory, sourceFileName).normalize();
            }

            validateSourceFile(sourcePath);
            ensureTargetDirectoryExists(targetDirectory);
            Path targetPath = copyFile(sourcePath, targetDirectory);
            System.out.println("Файл успешно скопирован: " + targetPath.toAbsolutePath());
        } catch (Exception e) {
            System.err.println("Ошибка при копировании файла: " + e.getMessage());
        }
    }

    /**
     * Разбирает строку команды на аргументы, учитывая двойные кавычки как ограничители строк.
     *
     * @param command Полная строка команды (например, "copy \"my file.txt\" \"target dir\"")
     * @return Список аргументов (например, ["copy", "my file.txt", "target dir"])
     */
    private List<String> parseArguments(String command) {
        List<String> arguments = new ArrayList<>();
        StringBuilder currentArg = new StringBuilder();
        boolean insideQuotes = false;
        boolean escaped = false;

        for (int i = 0; i < command.length(); i++) {
            char c = command.charAt(i);

            if (escaped) {
                currentArg.append(c);
                escaped = false;
                continue;
            }

            if (c == '\\') {
                if (i + 1 < command.length() && command.charAt(i + 1) == '"') {
                    escaped = true;
                    currentArg.append(command.charAt(i + 1)); // Добавляем '"' как обычный символ
                    i++;
                    continue;
                } else {
                    currentArg.append(c);
                    continue;
                }
            }


            if (c == '"') {
                insideQuotes = !insideQuotes;
            } else if (c == ' ' && !insideQuotes) {
                // Если встречаем пробел вне кавычек, это разделитель аргументов
                if (currentArg.length() > 0) {
                    arguments.add(currentArg.toString());
                    currentArg.setLength(0);
                }
            } else {
                // Добавляем символ к текущему аргументу
                currentArg.append(c);
            }
        }

        // Не забываем добавить последний аргумент, если он есть
        if (currentArg.length() > 0) {
            arguments.add(currentArg.toString());
        }

        return arguments;
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
            String baseName = fileName.lastIndexOf('.') > -1 ? fileName.substring(0, fileName.lastIndexOf('.')) : fileName;
            String extension = fileName.lastIndexOf('.') > -1 ? fileName.substring(fileName.lastIndexOf('.')) : "";
            targetPath = targetDirectory.resolve(baseName + "_" + counter + extension);
            counter++;
        }

        Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
        return targetPath;
    }
}