/**
 * Вспомогательный класс для форматирования размера файла.
 * <p>
 * Содержит статические методы для преобразования размера в байтах в человеко-читаемый формат (B, KB, MB, GB).
 * </p>
 */
public class FileSizeFormatter {

    /**
     * Конструктор класса FileSizeFormatter.
     * Этот класс содержит только статические методы и не требует экземпляра.
     */
    public FileSizeFormatter() {
        // Конструктор по умолчанию
    }

    /**
     * Форматирует размер файла в удобочитаемый вид.
     *
     * @param sizeInBytes размер файла в байтах
     * @return строка с форматированным размером (например, "1024 B", "1.00 KB", "2.50 MB")
     */
    public static String formatFileSize(long sizeInBytes) {
        if (sizeInBytes < 1024L) {
            return sizeInBytes + " B";
        } else if (sizeInBytes < 1024L * 1024L) {
            return String.format("%.2f KB", sizeInBytes / 1024.0);
        } else if (sizeInBytes < 1024L * 1024L * 1024L) {
            return String.format("%.2f MB", sizeInBytes / (1024.0 * 1024.0));
        } else {
            return String.format("%.2f GB", sizeInBytes / (1024.0 * 1024.0 * 1024.0));
        }
    }
}