CREATE TABLE Caregivers (
    Username varchar(255),
    Salt BINARY(16),
    Hash BINARY(16),
    PRIMARY KEY (Username)
);

CREATE TABLE Availabilities (
    Time date,
    Username varchar(255) REFERENCES Caregivers,
    isReserved int,
    PRIMARY KEY (Username)
);

CREATE TABLE Vaccines (
    Name varchar(255),
    Doses int,
    PRIMARY KEY (Name)
);

CREATE TABLE Patients (
    Username varchar(255),
    Salt BINARY(16),
    Hash BINARY(16),
    PRIMARY KEY (Username)
);

CREATE TABLE Appointment (
    ID varchar(255) PRIMARY KEY,
    Patient_Name varchar(255) REFERENCES Patients,
    Caregiver_Name varchar(255) REFERENCES Caregivers,
    Vaccine_Name varchar(255) REFERENCES Vaccines,
    Time date
);

