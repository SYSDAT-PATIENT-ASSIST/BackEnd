# System Development F25 – Patient Assist – Backend

## Changes in development 

### Team L
Pair Programming with Marcus – cph-me313@cphbusiness.dk


## Configuration

Main configuration file:  
📄 `src/main/resources/config.properties`

This file contains the database connection URL, username, and password used by Hibernate.  
Make sure to adjust these settings for your local or production environment.

---

## Database: How to Populate Data

You can populate the database using the following classes in the codebase:

### Step 1: Populate Ingredient Types

Run this class first to insert all predefined ingredient types into the database:

```java
PopulateIngredientType.main()
```

### Step 2: Populate Sample Data

Then run this class to insert dishes, recipes, and ingredients:

```java
PopulateDatabase.main()
```

This populates the system with example dishes, including nutritional values, allergens, and recipes.

> 💡 Make sure your database is running and `config.properties` points to the correct environment.

---



---

## Links


---
