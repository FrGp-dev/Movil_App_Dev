package com.example.directorioempresas.data

class CompanyRepository(private val companyDao: CompanyDao) {

    // Usa un Flow para obtener datos de forma reactiva
    fun getAllCompanies() = companyDao.getAllCompanies()

    fun getFilteredCompanies(query: String) = companyDao.getFilteredCompanies(query)

    suspend fun saveCompany(company: Company) {
        companyDao.insertOrUpdate(company)
    }

    suspend fun deleteCompany(company: Company) {
        companyDao.delete(company)
    }
}