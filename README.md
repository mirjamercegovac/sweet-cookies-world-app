# Sweet Cookies World App 
Android aplikacija koja omogućuje pregled recepata za kolače i slastice, izrađena u Android Studiju koristeći Kotlin. Korisnicima omogućuje pretraživanje i spremanje recepata koji su organizirani u kategorije i dostupni u PDF formatu unutar aplikacije, dok administratori imaju potpunu kontrolu nad sadržajem aplikacije.

## Korisničke uloge
- **Neregistrirani korisnik** - pristup samo početnom ekranu
- **Registrirani korisnik** - pregled recepata, favoriti, pretraživanje, uređivanje profila
- **Admin** - dodavanje kategorija i recepata (PDF), uređivanje/brisanje

## Funkcionalnosti
- Autentifikacija: registracija, prijava i resetiranje lozinke
- Pregled recepata po kategorijama
- Pretraživanje recepata i kategorija
- Označavanje recepata kao favoriti
- Pregled PDF recepata unutar aplikacije
- Uređivanje profila (ime, profilna slika)
- Admin panel za dodavanje, uređivanje i brisanje kategorija i recepata

## Tehnologije
- Kotlin
- Jetpack Compose
- Firebase Authentication
- Firebase Realtime Database
- Firebase Storage
- ViewBinding
- Glide (za učitavanje slika)
- PDFView (za prikaz recepata)

## Struktura baze podataka
- **Categories** - id, category, categoryImage, timestamp, uid
- **Recipes** -  id, categoryId, description, title, uid, url (PDF), viewsCount, timestamp
- **Users** - uid, name, email, profileImage, userType (user / admin), timestamp 
  - **Favourites** - recipeId, timestamp

## Testni podaci za prijavu

### Korisnik
- **Email**: user@gmail.com
- **Lozinka**: user12345

### Administrator
- **Email**: admin@gmail.com
- **Lozinka**: admin12345

### Kloniranje
```
git clone https://github.com/mirjamercegovac/sweet-cookies-world-app.git
```

### Dodavanje na postojeći repozitorij
```
cd existing_repo
git remote add origin https://github.com/mirjamercegovac/sweet-cookies-world-app.git
git branch -M main
git push -uf origin main
```
