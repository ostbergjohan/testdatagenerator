# Test Data Generator: Generate Random Swedish Personal Identity Numbers (Personnummer) and Profiles

This application generates random test data in CSV format, including valid Swedish personal identity numbers (personnummer). The last digit of each personal identity number is calculated using the **Luhn algorithm**, ensuring compliance with Swedish standards.

## Features

- Generates CSV-formatted test data.
- Includes valid Swedish personal identity numbers (personnummer).
- Supports user-defined parameters for the number of profiles and age ranges.
- Provides realistic personal and professional data, including names, addresses, phone numbers, and more.

## Endpoint

### `GET /generateTestdataCsv`

Generates random test data with the following fields for each profile.

### Input Parameters

| Parameter    | Type    | Required | Default | Description                                     |
|--------------|---------|----------|---------|------------------------------------------------|
| `count`      | String  | Yes      | None    | The number of profiles to generate.           |
| `minAge`     | String  | No       | `18`    | Minimum age for generated profiles.           |
| `maxAge`     | String  | No       | `65`    | Maximum age for generated profiles.           |

### Example Request

```http
GET /generateTestdataCsv?count=2&minAge=25&maxAge=40
```
## Output Format

### Content Type
- **CSV (text/csv)**

### Response Body
The response contains randomly generated test data formatted as CSV. Each line represents an individual's data, and the fields are separated by semicolons (`;`).

### CSV Structure
The first row serves as a header, and subsequent rows contain the data. Fields included are:

- **shortid1**: Short ID (e.g., `591125-3291`).
- **shortid2**: Variant of the short ID (e.g., `5911253291`).
- **longid1**: Long ID with birthdate and separator (e.g., `19591125-3291`).
- **longid2**: Variant of the long ID without separators (e.g., `195911253291`).
- **birthday**: Date of birth in `YYYY-MM-DD` format (e.g., `1959-11-25`).
- **firstname**: First name (e.g., `Lennart`).
- **lastname**: Last name (e.g., `Larsson`).
- **address**: Residential address (e.g., `Övre Fabriksvägen 20`).
- **postaladdress**: Region or area (e.g., `Halland`).
- **zip**: Postal code (e.g., `22969`).
- **phone**: Landline number (e.g., `917603114`).
- **mobilphone**: Mobile number (e.g., `0735074106`).
- **jobposition**: Job title or role (e.g., `Designer`).
- **jobtitel**: Detailed job description (e.g., `District Marketing Planner`).
- **email**: Randomized email (e.g., `sjxxic394769@kihtij249753.com`).
- **uuid**: Unique identifier in UUID format (e.g., `f03f3dde-be25-4459-9221-f8740406068b`).

### Example Response

```csv
shortid1;shortid2;longid1;longid2;birthday;firstname;lastname;address;postaladdress;zip;phone;mobilphone;jobposition;jobtitel;email;uuid
591125-3291;5911253291;19591125-3291;195911253291;1959-11-25;Lennart;Larsson;Övre Fabriksvägen 20;Halland;22969;917603114;0735074106;Designer;District Marketing Planner;sjxxic394769@kihtij249753.com;f03f3dde-be25-4459-9221-f8740406068b
690705-8645;6907058645;19690705-8645;196907058645;1969-07-05;Hans;Änglund;Lennarts Väg 24;Skåne;64984;457837500;0731807980;Consultant;Internal Education Consultant;hyyjfj880250@ooxlmk992126.com;e1d80578-c9dd-4e8d-90dd-ece6af423a0a
```
## Postal Address and ZIP Code Generation

### Overview
The **postal address** and **ZIP code** are fetched from a static file called `postnummer.csv`. This file contains a list of postal codes along with their associated area names and municipalities in Sweden. Each entry in the file is a combination of ZIP code, postal area (city or locality), and the municipality.

### CSV Format
The `postnummer.csv` file contains rows with the following format:


- **ZIP_CODE**: The postal code (e.g., `293 93`).
- **POSTAL_AREA**: The name of the area (e.g., `Gränum`).
- **MUNICIPALITY**: The name of the municipality (e.g., `Olofströms kommun`).

### Example Entries

```csv
293 93;Gränum;Olofströms kommun
293 94;Olofström;Olofströms kommun
293 95;Vilshult;Olofströms kommun
294 07;Sölvesborg;Sölvesborgs kommun
294 31;Sölvesborg;Sölvesborgs kommun
294 32;Sölvesborg;Sölvesborgs kommun
.....
```

## Personal Identity Number Generation

### Overview
- **Date of Birth**: Personal identity numbers are generated with a date of birth based on the specified age range (`minAge` and `maxAge`).
- **Serial Number**: A serial number is randomly assigned according to gender rules.
- **Control Digit**: The control digit is automatically calculated using the **Luhn algorithm**.

### What is a Swedish Personal Identity Number?

A Swedish personal identity number (**personnummer**) is a unique identifier for individuals in Sweden. It follows a specific structure:

#### Format
`YYMMDD-XXXX` or `YYYYMMDD-XXXX`

- **YYMMDD/YYYYMMDD**: The date of birth in the format `year-month-day`.
- **XXXX**:
  - The first three digits form a serial number, with the **second-to-last digit indicating gender**:
    - **Odd**: Male
    - **Even**: Female
  - The **last digit** is a control digit, calculated using the **Luhn algorithm**.

---

### Luhn Algorithm for the Control Digit

The control digit is calculated as follows:

1. **Multiply every other digit by 2, starting from the right (excluding the control digit).**
   - If the result is a two-digit number, add the digits together (e.g., `14 → 1 + 4 = 5`).
2. **Add all the digits together** (both multiplied and unmultiplied).
3. **Round the total up to the nearest number divisible by 10.**
4. The **control digit** is the difference needed to reach that number.

---

### Example Calculation

For the partial personal identity number `850709-123X`:

1. **Digits**: `8, 5, 0, 7, 0, 9, 1, 2, 3`
2. **Multiply every other digit by 2**:
   - `8 → 16 → 1 + 6 = 7`
   - `0 → 0`
   - `0 → 0`
   - `1 → 2`
   - `3 → 6`
3. Add the resulting values:  
   **7 + 5 + 0 + 7 + 0 + 9 + 2 + 2 + 6 = 38**
4. Round 38 up to the nearest multiple of 10: **40**
5. Calculate the control digit:  
   **40 - 38 = 2**

**Final personal identity number**: `850709-1232`
