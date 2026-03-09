package com.enterprises_management.enterprise.domain.models;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Third {
	private Long thId;
	private String entId;
	private String names;
	private String lastNames;
	private Long idNumber;
	private Boolean state;
	private String address;
	private String phoneNumber;
	private String email;
}
