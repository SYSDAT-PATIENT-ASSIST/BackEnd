# System Development F25 – Patient Assist – Backend

## Tech Stack

---

## Configuration

Main configuration file:  
📄 `src/main/resources/config.properties`

This file contains the database connection URL, username, and password used by Hibernate.  
Make sure to adjust these settings for your local or production environment.

---


## Changes in development

### Team L:
Pair Programming with Marcus – cph-me313@cphbusiness.dk

ER DIAGRAM for US 5.1 & US 11:
https://www.plantuml.com/plantuml/umla/dLNHQkCm47pNLwYvbnQkK0voS7dgVvIhKQnjibIMXBHk6ux--wsiE7OLkGsrmKIxixExuy6vZwG2zPqHuehsq87qHg4LvBma9xUEo7LIOu2Mq5aHdz5w5LGd3LgWW-S2Pmxf84HhL8ooSK53aF-4b78Bka6zaMW9TX1Io3g2ZNnHeTshSFrhlRv9qQ6c8UmubvuyLkUlVmyv_arzFA-P4xyVcskC-znQlIWqQckWUGgksqYj23x6UqjefaIhmjRj3phmcRjfrpD8UjuSOrgT8HfY6z0d9Sx7UL9KhtfkbQbdhEegMlZ04g6jboo9aJ0UIh6N96gKCH1sO6CMQuWLjIEaw3NIeDq5q0YMCiN202TKaynooNn5IMQlArKhpqwQV51RBMDgrNcBN8asKkW7txFW16-KeeHaAgFKsbGRE0EnkjFTf6qVURmPSZYN4kJNpg3cSw8ZX-VpAoz-Fde2dPE_2p68EBzk7twi_wJxv_2L-7lxEBxt2REHgyaAg5UYy5kTfMp5BAEYYMDcjkL9IkCmHyL-5zDzkTZCQSiinw_XdENalej9lb2ddFGx73FBVIB9B3zASZ9WUbp8astvWMUo-6Z7bRW7gzDVqty0


---

## Database: How to Populate Data

You can populate the database using the following classes in the codebase. This populates the system with example dishes, including nutritional values, allergens, and recipes. We also have users, roles, and orders.

> 💡 Make sure your database is running and `config.properties` points to the correct environment.


#### Step 1: Populate Ingredient Types

Run this class first to insert all predefined ingredient types into the database:

```java
PopulateIngredientType.main();
```

#### Step 2: Populate Sample Data

Then run this class to insert dishes, recipes, and ingredients:

```java
PopulateDatabase.main();
```
---



---

## Group Links


---

## Group Files

---