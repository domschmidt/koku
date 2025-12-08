import java.sql.Connection;

public class CustomerMigration extends BaseMigration {

    public CustomerMigration(Connection source, Connection target) {
        super(source, target);
    }

    @Override
    public void migrate() throws Exception {
        System.out.println("Migrating Customer...");

        read("""
                SELECT  id, recorded, updated,
                    	additional_info, address, business_telephone_no, city, email, first_name, last_name,
                    	medical_tolerance, mobile_telephone_no, postal_code, private_telephone_no, birthday,
                    	on_first_name_basis, deleted, hay_fever, glasses, epilepsy, dry_eyes, diabetes,
                    	cyanoacrylate_allergy, contacts, claustrophobia, circulation_problems, asthma,
                    	plaster_allergy, neurodermatitis, eye_disease, allergy, covid19vaccinated,
                    	covid19boostered
                FROM koku.customer
            """, rs -> {
            try {
                exec("""
                        INSERT INTO koku.customer (external_ref, recorded, updated,
                            firstname, lastname, email, address, postal_code, city, private_telephone_no,
                            business_telephone_no, mobile_telephone_no, medical_tolerance, additional_info,
                            on_firstname_basis, hay_fever, plaster_allergy, cyanoacrylate_allergy,
                            asthma, dry_eyes, circulation_problems, epilepsy, diabetes, claustrophobia,
                            neurodermatitis, contacts, glasses, covid19vaccinated, covid19boostered,
                            eye_disease, allergy, birthday
                        )
                        VALUES (?, COALESCE(?, ?, CURRENT_TIMESTAMP), ?, 
                            COALESCE(?, ''), COALESCE(?, ''), COALESCE(?, ''), COALESCE(?, ''), COALESCE(?, ''), COALESCE(?, ''), COALESCE(?, ''), COALESCE(?, ''), COALESCE(?, ''), COALESCE(?, ''), COALESCE(?, ''), ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, COALESCE(?, ''), COALESCE(?, ''), ?
                        )
                        ON CONFLICT (external_ref)
                        DO UPDATE SET recorded = COALESCE(EXCLUDED.recorded, EXCLUDED.updated, CURRENT_TIMESTAMP),
                                      updated = EXCLUDED.updated,
                                      deleted = EXCLUDED.deleted,
                                      firstname = EXCLUDED.firstname,
                                      lastname = EXCLUDED.lastname,
                                      email = EXCLUDED.email,
                                      address = EXCLUDED.address,
                                      postal_code = EXCLUDED.postal_code,
                                      city = EXCLUDED.city,
                                      private_telephone_no = EXCLUDED.private_telephone_no,
                                      business_telephone_no = EXCLUDED.business_telephone_no,
                                      mobile_telephone_no = EXCLUDED.mobile_telephone_no,
                                      medical_tolerance = EXCLUDED.medical_tolerance,
                                      additional_info = EXCLUDED.additional_info,
                                      on_firstname_basis = EXCLUDED.on_firstname_basis,
                                      hay_fever = EXCLUDED.hay_fever,
                                      plaster_allergy = EXCLUDED.plaster_allergy,
                                      cyanoacrylate_allergy = EXCLUDED.cyanoacrylate_allergy,
                                      asthma = EXCLUDED.asthma,
                                      dry_eyes = EXCLUDED.dry_eyes,
                                      circulation_problems = EXCLUDED.circulation_problems,
                                      epilepsy = EXCLUDED.epilepsy,
                                      diabetes = EXCLUDED.diabetes,
                                      claustrophobia = EXCLUDED.claustrophobia,
                                      neurodermatitis = EXCLUDED.neurodermatitis,
                                      contacts = EXCLUDED.contacts,
                                      glasses = EXCLUDED.glasses,
                                      covid19vaccinated = EXCLUDED.covid19vaccinated,
                                      eye_disease = EXCLUDED.eye_disease,
                                      allergy = EXCLUDED.allergy,
                                      birthday = EXCLUDED.birthday,
                                      version = customer.version + 1
                        WHERE EXCLUDED.updated > customer.updated;
                        """,
                        rs.getString("id"),
                        rs.getTimestamp("recorded"),
                        rs.getTimestamp("updated"),
                        rs.getTimestamp("updated"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("email"),
                        rs.getString("address"),
                        rs.getString("postal_code"),
                        rs.getString("city"),
                        rs.getString("private_telephone_no"),
                        rs.getString("business_telephone_no"),
                        rs.getString("mobile_telephone_no"),
                        rs.getString("medical_tolerance"),
                        rs.getString("additional_info"),
                        rs.getBoolean("on_first_name_basis"),
                        rs.getBoolean("hay_fever"),
                        rs.getBoolean("plaster_allergy"),
                        rs.getBoolean("cyanoacrylate_allergy"),
                        rs.getBoolean("asthma"),
                        rs.getBoolean("dry_eyes"),
                        rs.getBoolean("circulation_problems"),
                        rs.getBoolean("epilepsy"),
                        rs.getBoolean("diabetes"),
                        rs.getBoolean("claustrophobia"),
                        rs.getBoolean("neurodermatitis"),
                        rs.getBoolean("contacts"),
                        rs.getBoolean("glasses"),
                        rs.getBoolean("covid19vaccinated"),
                        rs.getBoolean("covid19boostered"),
                        rs.getString("eye_disease"),
                        rs.getString("allergy"),
                        rs.getDate("birthday")

                );
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        System.out.println("✔ Customer done.");
    }
}
