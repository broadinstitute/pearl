package bio.terra.pearl.core.service.datarepo;

import bio.terra.datarepo.client.ApiException;
import bio.terra.pearl.core.dao.datarepo.DataRepoDao;
import org.springframework.stereotype.Service;

@Service
public class DataRepoExportService {

    DataRepoDao dataRepoDao;

    public DataRepoExportService(DataRepoDao dataRepoDao) {
        this.dataRepoDao = dataRepoDao;
    }

    public boolean getServiceStatus() {
        try {
            return dataRepoDao.getServiceStatus().isOk();
        } catch (ApiException e) {
            return false;
        }
    }

    public String makeDatasetName(String deploymentZone, String studyName, String environmentName) {
        if(deploymentZone.equalsIgnoreCase("prod"))
            return String.format("d2p_%s_%s", studyName, environmentName);
        else
            return String.format("d2p_%s_%s_%s", deploymentZone, studyName, environmentName);
    }


}
