package com.my_app.page.company.save.service;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.struts.action.ActionMessage;

import com.my_app.model.Company;
import com.my_app.page.company.save.CompanySaveForm;
import com.my_app.page.company.save.mapper.CompanySaveFormToCompanyMapper;
import com.my_app.page.company.save.mapper.CompanyToCompanySaveFormMapper;
import com.my_app.service.CityService;
import com.my_app.service.CompanyService;
import com.my_app.service.CountryService;
import com.my_app.utils.UserUtils;

public class CompanySaveServiceImpl implements CompanySaveService {

	final CompanyService companyService;
	final CountryService countryService;
	final CityService cityService;

	public CompanySaveServiceImpl(CompanyService companyService, CountryService countryService, CityService cityService) {
		super();
		this.companyService = companyService;
		this.countryService = countryService;
		this.cityService = cityService;
	}

	@Override
	public void formInit(CompanySaveForm form) {
		if (!form.isNewUser()) {
			final Company company = this.companyService.findById(form.getCompanyId());

			new CompanyToCompanySaveFormMapper().mapTo(company, form);
			form.setOriginalUsername(form.getname());
		}
	}

	@Override
	public boolean validate(CompanySaveForm form) {
		boolean isValid = true;

		if (StringUtils.isBlank(form.getname())) {
			isValid = false;
			form.getActionErrors().add("name", new ActionMessage("error.common.required"));
		} else if ((form.isNewUser() || (!form.isNewUser()
				&& !form.getOriginalUsername().equals(UserUtils.normalizeUsername(form.getname()))))
				&& companyService.findByName(form.getname()) != null) {
			isValid = false;
			form.getActionErrors().add("name", new ActionMessage("company.name-taken.error"));
		}

		if (StringUtils.isBlank(form.getAdress())) {
			isValid = false;
			form.getActionErrors().add("adress", new ActionMessage("error.common.required"));
		}

		if (form.getCountryId() == null || form.getCountryId() == 0) {
			isValid = false;
			form.getActionErrors().add("country", new ActionMessage("error.common.required"));
		}

		if (form.getCityId() == null || form.getCityId() == 0) {
			isValid = false;

			if (form.getCountryId() == null || form.getCountryId() == 0) {
				form.getActionErrors().add("city", new ActionMessage("form.field.pre-choose", "Country"));
			} else {
				form.getActionErrors().add("city", new ActionMessage("error.common.required"));
			}
		}
		
		
		
		if ((form.getIva() < 0) || (form.getIva() == 0)) {
			isValid = false;
			form.getActionErrors().add("iva", new ActionMessage("error.iva.zero.or.negative"));
			
		} 
		
		

		return isValid;
	}

	@Override
	public Company saveCompany(CompanySaveForm form) {
		return this.companyService.save(new CompanySaveFormToCompanyMapper().toCompany(form));
	}

	@Override
	public void setRequestAttrs(CompanySaveForm form, HttpServletRequest req) {
		req.setAttribute("countries", this.countryService.findAll());

		if (form.getCountryId() != null && form.getCountryId() > 0) {
			req.setAttribute("cities", this.cityService.findAllByCountryId(form.getCountryId()));
		}
	}

}
