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
```

### Обзор работы с классом JsonToStringStyle

В данном примере присутствует зависимость <b>Apache Commons Lang 3</b>
</br>[Скачать с Maven Repository](https://mvnrepository.com/artifact/org.apache.commons/commons-lang3)

```java
import org.apache.commons.lang3.builder.ToStringBuilder;
import ru.hzerr.util.JsonToStringStyle;

public class Main {
    
    public static void main(String[] args) {
        Person person = new Person("Devarov", "email@gmail.com");
        System.out.println(person);
        /**
         * Print
         * ru.hzerr.Main$Person {
         * 	name=Devarov,
         * 	email=email@gmail.com
         * }
         */
    }

    private static class Person {

        private String name;
        private String email;

        public Person(String name, String email) {
            this.name = name;
            this.email = email;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this, new JsonToStringStyle())
                    .append("name", name)
                    .append("email", email)
                    .toString();
        }
    }
}

