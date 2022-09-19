Пакет ru.hzerr.file. Работа с файлами и замена java.io.File
=====================================

Данный пакет предлагает два класса: <b>HDirectory(представление директории)</b> и <b>HFile(представление обычного файла)</b>.

### Обзор работы с классом HDirectory

```java
import java.io.IOException;

import ru.hzerr.file.*;
import ru.hzerr.collection.*;
import ru.hzerr.util.*;

public class Main {
    public static void main(String... args) throws IOException {
        // Доступные варианты создания экземпляра класса
        BaseDirectory userHome = new HDirectory(SystemInfo.getUserHome());
        BaseDirectory downloads = new HDirectory(userHome, "Downloads");
        BaseDirectory music = new HDirectory(downloads.getLocation(), "Music");
        BaseDirectory music2 = new HDirectory(music.asURI()); // Этим конструктором ни разу в жизни не пользовался
        // BaseDirectory xxx = new HDirectory(music, "music.mp3"); // throws ValidationException
      
        // Методы из интерфейса IBackwardCompatibility (также имеются и в классе HFile)
        music.asPath(); // return java.nio.file.Path, представляющий данный каталог
        music.asIOFile(); // return java.io.file, представляющий данный каталог
        music.asURI(); // return java.net.URI, представляющий данный каталог
        music.asURL(); // return java.net.URL, представляющий данный каталог
        // Методы из интерфейса IObject (также имеются и в классе HFile)
        music.notEquals(downloads); // return true
        // Методы из интерфейса IFSObject (также имеются и в классе HFile)
        downloads.getLocation(); // return C:\Users\HZERR\Downloads
        downloads.getName(); // return Downloads
        BaseDirectory xxx = new HDirectory("C:\\Windows\\xxx");
        // xxx.create(); // throws HDirectoryCreateImpossibleException
        music.exists(); // return true
        music.notExists(); // return false
        music.create(); // NO THROWS. Если директория существует, метод create ничего не делает
        // music.delete(); // Удаляет полностью директорию
        music.rename("Electronic Music"); // Переименовывает директорию(пока занимает продолжительное время, поправим)
        music.deleteOnExit(); // Полностью удаляет директорию при завершении программы
        music.getParent(); // return [BaseDirectory] downloads
        // new HDirectory("C:\\").getParent(); // throws ParentNotFoundException
        music.isHierarchicalChild(downloads); // return true
        music.isHierarchicalChild(userHome); // return true
        userHome.notIsHierarchicalChild(music); // return true
        downloads.notIsHierarchicalChild(music); // return true
        /* C:/Program Files/BellSoft/LibericaJDK-8-Full/bin/java.exe
         * В данной ситуации java.exe, bin, LibericaJDK-8-Full, BellSoft, Program Files будут иерархическими детьми по отношению к C:/
         * java.exe, bin, LibericaJDK-8-Full, BellSoft по отношению к Program Files
         * java.exe, bin, LibericaJDK-8-Full по отношению к BellSoft и тд
         */
        downloads.sizeOf(SizeType.MB); // return [double] 13425.1
        downloads.sizeOf(SizeType.GB); // return [double] 13.1
        downloads.sizeOfAsBigDecimal(SizeType.BYTE); // return [java.math.BigDecimal] 14077275493.0
        // Методы из абстрактного класса BaseDirectory
        BaseDirectory electronic = music.createSubDirectory("electronic"); // Создает подкаталог
        BaseFile rein = electronic.createSubFile("rein.mp3"); // создает подфайл
        // BaseDirectory rein2 = electronic.getSubDirectory("rein.mp3"); // throws ValidationException
        electronic = music.getSubDirectory("electronic");
        electronic.create(); // ничего не делает
        rein = electronic.getSubFile("rein.mp3");
        rein.create(); // ничего не делает
        electronic.getFiles(); // return HList<BaseFile> | НЕ РЕКУРСИВНЫЙ
        electronic.getDirectories(); // return HList<BaseDirectory> | НЕ РЕКУРСИВНЫЙ
        // Не рекурсивно выводит путь для всех файлов/директорий
        electronic.getAllFiles(false).map(IFSObject::getLocation).forEach(System.out::println);
        // Рекурсивно выводит путь для всех файлов/директорий
        electronic.getAllFiles(true).map(IFSObject::getLocation).forEach(System.out::println);
        // Рекурсивно выводит путь для всех файлов
        electronic.getFiles(true).map(IFSObject::getLocation).forEach(System.out::println);
        // Рекурсивно выводит путь для всех директорий
        electronic.getDirectories(true).map(IFSObject::getLocation).forEach(System.out::println);
        // Не рекурсивно выводит путь для всех файлов, кроме rein.mp3
        electronic.getFilesExcept(rein).map(IFSObject::getLocation).forEach(System.out::println);
        // Не рекурсивно выводит путь для всех файлов, кроме rein.mp3. ОБЯЗАТЕЛЬНО ПИСАТЬ РАСШИРЕНИЕ ФАЙЛА
        electronic.getFilesExcept("rein.mp3").map(IFSObject::getLocation).forEach(System.out::println);
        // Рекурсивно выводит путь для всех файлов, кроме rein.mp3
        electronic.getFilesExcept(true, rein).map(IFSObject::getLocation).forEach(System.out::println);
        /*
         * Аналогично с методами:
         * getDirectoriesExcept(BaseDirectory... dirsToBeExcluded)
         * getDirectoriesExcept(String... dirsToBeExcluded) | РАСШИРЕНИЯ ДЛЯ ФАЙЛОВ АНАЛОГИЧНО ПИСАТЬ НЕ НУЖНО
         * getDirectoriesExcept(boolean recursive, BaseDirectory... dirsToBeExcluded)
         * getAllFilesExcept(HList<IFSObject> filesToBeExcluded, boolean recursive)
         * deleteExcept(BaseDirectory... directories) | РЕКУРСИВНЫЙ
         * deleteExcept(BaseFile... files) | РЕКУРСИВНЫЙ
         * deleteExcept(HList<IFSObject> excludedFiles) | РЕКУРСИВНЫЙ
         * В верхнем случае список представляет собой совокупность BaseDirectory и BaseFile. 
         * Например:
         * HList<IFSObject> files = HList.of(electronic, rein);
         */
        // Возвращает true если каталог пустой
        electronic.isEmpty(); // return false
        // Возвращает true если каталог не пустой
        electronic.isNotEmpty(); // return true
        // Возвращает true если в каталоге присутствуют только файлы. Проверяет не рекурсивно
        electronic.hasOnlyFiles(); // return true
        // Возвращает true если в каталоге присутствуют только каталоги. Проверяет не рекурсивно
        electronic.hasOnlyDirectories(); // return false
        // Возвращает true если в каталоге не найдены другие каталоги. Проверяет не рекурсивно
        electronic.notFoundInternalDirectories(); // return true
        // Возвращает true если в каталоге не найдены файлы. Проверяет не рекурсивно
        electronic.notFoundInternalFiles(); // return false
        // Проверяет рекурсивно/не рекурсивно содержит ли данный каталог конкретный файл
        electronic.contains(rein, false); // return true
        // Находит РЕКУРСИВНО все файлы/директории, которые соответствуют условию
        electronic.find(ifsObject -> ifsObject.getLocation().endsWith(".mp3")).map(IFSObject::getLocation).forEach(System.out::println);
        // Аналогично предыдущему. Писать glob: НЕ НУЖНО. Подробнее см. Java Glob: https://docs.oracle.com/javase/tutorial/essential/io/fileOps.html#glob
        electronic.find("**/*.mp3").map(IFSObject::getLocation).forEach(System.out::println);
        // Находит РЕКУРСИВНО все файлы/директории, которые соответствуют именам. Названия файлов писать с расширением
        electronic.findByNames("rein.mp3", "Additional Music").map(IFSObject::getLocation).forEach(System.out::println);
        /*
         * Аналогично с методами:
         * findDirectories(Predicate<? super BaseDirectory> matcher)
         * findDirectories(String glob)
         * findDirectoriesByNames(String... names)
         * findFiles(Predicate<? super BaseFile> matcher)
         * findFiles(String glob)
         * findFilesByNames(String... names) Названия файлов писать с расширением
         * ВСЕ ЭТИ МЕТОДЫ ИЩУТ РЕКУРСИВНО
         */
        // Очищает каталог, но не удаляет его
        electronic.clean();
        // electronic.delete("rein"); // throws NoSuchFileException
        // Не рекурсивно ищет и удаляет файл или каталог
        electronic.delete("rein.mp3");
        // Копирует "себя" в другой каталог (возможно долгая операция, поправим)
        electronic.copyToDirectory(downloads);
        // Копирует содержимое в другой каталог (возможно долгая операция, поправим)
        electronic.copyContentToDirectory(downloads);
        // Перемещает "себя" в другой каталог. 
        // Внутренне изменяет состояние. electronic будет иметь уже другой location
        electronic.moveToDirectory(downloads);
        // Перемещает содержимое в другой каталог. 
        // Внутренне изменяет состояние. electronic будет иметь уже другой location, который равен передаваемой директории
        // Если указано, что нужно удалить старую директорию, возвращается Optional.empty(), иначе Optional со старым BaseDirectory
        Optional<BaseDirectory> oldDirectory = electronic.moveContentToDirectory(downloads, false);
        // Не рекурсивно сверяет количество файлов/директорий
        electronic.checkCountFiles(1L); // false
        // Не рекурсивно сверяет количество файлов
        electronic.checkCountOnlyFiles(1L); // false
        // Не рекурсивно сверяет количество директорий
        electronic.checkCountOnlyDirectories(1L); // false
        rein.create();
        electronic.checkCountFiles(1L); // true
        electronic.checkCountOnlyFiles(1L); // true
        electronic.checkCountOnlyDirectories(1L); // false
        // Не рекурсивно проверяет наличие только 1 файла в каталоге. Если в каталоге еще присутствует директория, вернет false
        electronic.hasOnly1File(); // true
        electronic.hasOnly1File("rein"); // false
        electronic.hasOnly1File("rein.mp3"); // true
        // Не рекурсивно проверяет наличие только 1 каталога в текущем каталоге. Если в каталоге еще присутствует файл, вернет false
        electronic.hasOnly1Directory(); // false
        electronic.hasOnly1Directory(""); // false
        // Не рекурсивно проверяет наличие только 1 каталога/файла в текущем каталоге
        electronic.hasOnly1FileOrDirectory(); // true
    }
}
```

### Обзор работы с классом HFile

```java
import ru.hzerr.collections.list.HList;
import ru.hzerr.file.BaseDirectory;
import ru.hzerr.file.BaseFile;
import ru.hzerr.file.HDirectory;
import ru.hzerr.file.HFile;
import ru.hzerr.util.SystemInfo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class Main {
    public static void main(String... args) throws IOException {
        BaseDirectory downloads = new HDirectory(SystemInfo.getUserHome(), "Downloads");
        BaseFile yandexBrowser;
        yandexBrowser = new HFile(downloads.getLocation().concat(File.separator).concat("yandexXXX.exe"));
        yandexBrowser = new HFile(downloads.getLocation(), "yandexXXX.exe");
        yandexBrowser = new HFile(downloads, "yandexXXX.exe");
        yandexBrowser = new HFile(yandexBrowser.asURI());
        yandexBrowser.create();
        yandexBrowser.getExtension(); // return exe
        yandexBrowser.getBaseName(); // return yandexXXX
        yandexBrowser.getName(); // return yandexXXX.exe
        BaseFile torBrowser = new HFile(downloads, "tor.exe");
        torBrowser.create();
        yandexBrowser.equalsExtension(torBrowser); // return true
        yandexBrowser.notEqualsExtension(torBrowser); // return false
        yandexBrowser.equalsBaseName(torBrowser); // return false
        yandexBrowser.notEqualsBaseName(torBrowser); // return true
        // Записывает в файл строку. Перед записью уничтожает все содержимое файла
        // Кодировка записи по-умолчанию UTF-8
        yandexBrowser.write("Яндекс");
        // Записывает в файл строку в кодировке UTF-8. Перед записью уничтожает все содержимое файла
        yandexBrowser.write("Яндекс", StandardCharsets.UTF_8);
        // Уничтожает/не уничтожает содержимое и добавляет в начало/конец файла строку в кодировке UTF-8
        yandexBrowser.write(" Браузер", StandardCharsets.UTF_8, true);
        torBrowser.rename("yandex64XXX", "exe");
        // Оба файла обязательно должны существовать
        yandexBrowser.copyToFile(torBrowser); // Аналогично и для yandexBrowser.copyToDirectory(new HDirectory(SystemInfo.getUserHome()));
        torBrowser.readLines(StandardCharsets.UTF_8).forEach(System.out::println); // print Яндекс Браузер
        // Записывает в файл строки. Перед записью уничтожает все содержимое файла
        // Кодировка записи по-умолчанию UTF-8
        torBrowser.writeLines("1 line", "2 line", "3 line");
        torBrowser.writeLines(HList.of("1 line", "2 line", "3 line"));
        torBrowser.writeLines(HList.of("4 line", "5 line"), true);
        torBrowser.writeLines(HList.of("6 line"), StandardCharsets.UTF_8, true);
        torBrowser.readLines(StandardCharsets.UTF_8).forEach(System.out::println);
        /*
         * Output:
         * 1 line
         * 2 line
         * 3 line
         * 4 line
         * 5 line
         * 6 line
         */
        // Читает содержимое файла в массив
        torBrowser.readToByteArray(); // return byte[] array
        // Чтение файла из памяти
        // torBrowser.readFromMemory(StandardCharsets.UTF_8); // throws ByteBufferNotInitializationException
        torBrowser.refreshDataInMemory();
        // АСИНХРОННО НЕ ЧИТАТЬ. Используйте библиотеку RxIO
        torBrowser.readFromMemory(StandardCharsets.UTF_8).lastElement(); // return 6 line
        // Силами 7 богов пытается очистить буфер
        torBrowser.cleanDataInMemory();
        // Возвращает контрольную сумму файла, используя процедуру контрольной суммы CRC32
        torBrowser.checksum();
        // Открывает FileInputStream
        try (FileInputStream fis = torBrowser.openInputStream()) {
        }
        // Открывает FileOutputStream
        try (FileOutputStream fis = torBrowser.openOutputStream(true)) {
        }
        // Очищает файл
        torBrowser.clear();
        torBrowser.getLocation(); // return C:\Users\HZERR\Downloads\yandex64XXX.exe
        // Удалим dst файл, если таковой существует
        new HDirectory(SystemInfo.getUserHome()).getSubFile(torBrowser.getName()).deleteIfPresent();
        // Перемещаем файл yandex64XXX.exe в другую директорию. При перемещении состояние HDirectory также меняется
        torBrowser.moveToDirectory(new HDirectory(SystemInfo.getUserHome()));
        torBrowser.getLocation(); // return C:\Users\HZERR\yandex64XXX.exe
        torBrowser.write("TOR Browser");
        // Удалим dst файл, если таковой существует
        yandexBrowser.deleteIfPresent();
        torBrowser.moveToFile(yandexBrowser);
        torBrowser.getLocation(); // return C:\Users\HZERR\Downloads\yandexXXX.exe
        torBrowser.equals(yandexBrowser); // return true
        torBrowser.readLines(StandardCharsets.UTF_8).forEach(System.out::println); // TOR Browser
        // Остальные методы описаны в коде работы с HDirectory
    }
}
```
