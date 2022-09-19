Пакет ru.hzerr.util. Работа с утилитарными классами
=====================================

Данный пакет предлагает такие классы:
1. IOTools
2. Instruments
3. SystemInfo
4. JsonToStringStyle

### Обзор работы с классом IOTools

```java
import javafx.scene.image.Image;
import ru.hzerr.util.IOTools;

import java.io.IOException;

public class Main {
    public static void main(String... args) throws IOException {
        Image case = IOTools.getResourceAsStream("/image/logo.png", iStream -> {
              return new Image(iStream, 32, 32, false, false);
        });
        Image case2 = IOTools.getResourceAsStream("/image/logo.png", iStream -> {
            return new Image(iStream, 32, 32, false, false);
        }, Throwable::printStackTrace);
        IOTools.getResourceAsStream("/image/logo.png", iStream -> {
            // void process
        });
        IOTools.getResourceAsStream("/image/logo.png", iStream -> {
            // void process
        }, Throwable::printStackTrace);
    }
}
