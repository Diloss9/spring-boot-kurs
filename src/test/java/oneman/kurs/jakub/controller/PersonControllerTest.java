package oneman.kurs.jakub.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest
@AutoConfigureMockMvc
class PersonControllerTest {

	@Autowired
	private MockMvc mockMvc;


	@Test
	void checkForPersons() throws Exception {
		mockMvc.perform(get("/persons"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(3)))

				.andExpect(jsonPath("$[*].first_name").exists())
				.andExpect(jsonPath("$[*].last_name").exists())
				.andExpect(jsonPath("$[*].description").exists())
				.andExpect(jsonPath("$[*].date_of_birth").exists())

				.andExpect(jsonPath("$[*].date_of_birth").isNotEmpty())

				.andExpect(jsonPath("$[1].first_name", is("Max")))
				.andExpect(jsonPath("$[1].last_name", is("Mustermann")))
				.andExpect(jsonPath("$[1].date_of_birth", is("1997-06-23")));
	}

	@ParameterizedTest
	@CsvSource({
			"'{}'",
			"'{\"first_name\": null }'",
			"'{\"last_name\": null }'",
			"'{\"first_name\": \"\" }'",
			"'{\"last_name\": \"\" }'",
			"'{\"first_name\": \"A\" }'",
			"'{\"first_name\": \"A very long first name that exceeds the limit\" }'",
			"'{\"first_name\": \"John!@#\" }'",
			"'{\"last_name\": \"Doe!@#\" }'",
			"'{\"birth_of_date\": \"31-12-1991\" }'",
			"'{\"birth_of_date\": \"1991/12/31\" }'",
			"'{\"first_name\": \"John\", \"unknown_field\": \"value\" }'",
			"'{\"first_name\": \"John\", \"last_name\": \"Doe\", \"extra_field\": \"value\" }'",
			"'{\"first_name\": \"John\" }'",
			"'{\"first_name\": null, \"last_name\": \"Doe\" }'",
			"'{\"first_name\": \"John\", \"last_name\": null }'",
			"'{\"first_name\": \"John \"Doe\"\" }'",
			"'{\"birth_of_date\": \"2025-02-30\" }'",
			"'{\"birth_of_date\": \"9999-12-31\" }'",
			"'{\"birth_of_date\": \"0001-01-01\" }'",
			"'{\"birth_of_date\": \"text\" }'",
			"'{\"first_name\": 123 }'",
			"'{\"first_name\": \"123\", \"last_name\": \"456\" }'",
			"'{\"first_name\": \"John\", \"last_name\": \"Doe\" '",
			"'{\"first_name\": \"John\" , \"last_name\": \"Doe\", }'",
			"'{\"first_name\": \"John \"Doe\"\" }'"
		})

		void postInvalidPerson (String input) throws Exception {
			mockMvc.perform(post("/persons")
							.content(input)
							.contentType(MediaType.APPLICATION_JSON))
					.andExpect(status().isBadRequest());
		}

	@Test
	void postPersonAndVerify() throws Exception {
			mockMvc.perform(get("/persons"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()",is (3)));

		String validInput = """
        {
            "first_name": "Jakub",
            "last_name": "Waclawowicz"
        }
    """;

		MvcResult validResult = mockMvc.perform(post("/persons")
						.content(validInput)
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated())
				.andReturn();

		String validLocation = validResult.getResponse().getHeader(HttpHeaders.LOCATION);
		assertThat(validLocation).as("Location header should not be null").isNotNull();

		mockMvc.perform((get("/persons")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()",is (4)));

		mockMvc.perform(get(validLocation))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.first_name", is("Jakub")))
				.andExpect(jsonPath("$.last_name", is("Waclawowicz")));

		String invalidInput = """
        {
            "first_name": "John"
        }
    """;

		MvcResult invalidResult = mockMvc.perform(post("/persons")
						.content(invalidInput)
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest())
				.andReturn();

		String invalidLocation = invalidResult.getResponse().getHeader(HttpHeaders.LOCATION);
		assertThat(invalidLocation).as("Location header should be null").isNull();

		mockMvc.perform(get("/persons"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(4));
	}
}
