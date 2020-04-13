# configfile-checker

configfile-checker can be used to detect the values in property files:

```java
void a() throws IOException {
        Properties prop = new Properties();
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("a.property");
        prop.load(inputStream);
        @ConfigFilePropertyValue("http://www.example.com") String url = prop.getProperty("URL");
    }
```

In `a.property`:

```
URL=http://www.example.com
```
