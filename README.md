📦 Envanter Yönetimi Sistemi
Bu proje, Spring Boot tabanlı, kullanıcıların envanter kayıtlarını yönetebildiği, giriş/kayıt sistemine sahip bir web uygulamasıdır. Katmanlı mimari (n-tier architecture) prensipleriyle geliştirilmiştir.

🚀 Özellikler
Kullanıcı Yönetimi: Kayıt olma ve giriş yapma işlemleri.

Envanter Takibi: Ürünlerin/envanterlerin listelenmesi ve yönetilmesi.

Güvenlik: Şifrelerin BCryptPasswordEncoder ile güvenli bir şekilde saklanması.

Dinamik Arayüz: Spring Boot ile entegre edilmiş dinamik HTML sayfaları.

🛠️ Kullanılan Teknolojiler
Java 17+

Spring Boot 3.x

Spring Data JPA (Veritabanı işlemleri için)

Spring Security (Kimlik doğrulama için)

Spring Web (MVC yapısı için)

Veritabanı: PostgreSQL / MySQL (veya H2)

Arayüz: Thymeleaf / HTML / CSS

Build Aracı: Maven

->Projenin Yapısı
src/main/java/com/koleksiyon/envanter/
├── controller/   # Web isteklerini karşılayan katman
├── entity/       # Veritabanı tablolarını temsil eden sınıflar
├── repository/   # Veritabanı erişim arayüzleri (JPA)
├── service/      # İş mantığının (Business Logic) olduğu katman
└── dto/          # Veri transfer nesneleri (Data Transfer Objects)
->Kurulum ve Çalıştırma
1-Projeyi Kopyalayın
git clone https://github.com/ayseakdogann/EnvanterYonetimi2.git
2-application.properties dosyasındaki veritabanı ayarlarını kendi yerel ayarlarınıza göre güncelleyin.
3-Maven ile projeyi derleyin.
mvn clean install
    ```
4.  Uygulamayı başlatın:
    ```bash
    mvn spring-boot:run
    ```
5.  Tarayıcınızdan `http://localhost:8080/login` adresine gidin.

### 💡 Geliştirici Notu
Bu proje geliştirilmeye devam etmektedir. Gelecek güncellemelerde **LOB** (Large Object) desteği ile dosya/resim yükleme ve daha detaylı bir raporlama modülü eklenmesi planlanmaktadır.

### Küçük Bir Tavsiye
Dosyayı ekledikten sonra projendeki ekran görüntülerinden birini (örneğin login veya ana sayfa) bir `img` klasörüne atıp README içine şu şekilde eklersen çok daha etkileyici durur:
`![Ana Sayfa Ekran Görüntüsü](img/screenshot.png)`

Projene "Dashboard" gibi bir özellik eklemeyi düşünüyor musun, yoksa şu an temel CRUD işlemlerine mi odaklanıyorsun?

Projeyi Yapan Kişiler:
Ayşe Akdoğan
Şevvalsu Aktaş
Melih Ali Çağman
