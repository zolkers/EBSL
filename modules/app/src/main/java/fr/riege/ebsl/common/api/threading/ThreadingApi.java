package fr.riege.ebsl.common.api.threading;

import fr.riege.ebsl.common.api.core.annotation.EbslApiOperation;
import fr.riege.ebsl.common.api.core.annotation.EbslApiSurface;
import fr.riege.ebsl.common.core.threading.EbslExecutor;
import fr.riege.ebsl.common.core.threading.EbslThreadDomain;
import fr.riege.ebsl.common.core.threading.EbslThreadError;
import fr.riege.ebsl.common.core.threading.EbslThreadErrorLog;
import fr.riege.ebsl.common.core.threading.EbslThreading;

import java.util.List;

@EbslApiSurface(EbslApiSurface.Domain.THREADING)
public final class ThreadingApi {
    @EbslApiOperation("Read supported EBSL thread domains.")
    public EbslThreadDomain[] domains() {
        return EbslThreadDomain.values();
    }

    @EbslApiOperation("Access an executor for a thread domain.")
    public EbslExecutor executor(EbslThreadDomain domain) {
        return EbslThreading.executor(domain);
    }

    @EbslApiOperation("Read recent thread failures.")
    public List<EbslThreadError> errors() {
        return EbslThreadErrorLog.snapshot();
    }

    @EbslApiOperation("Clear recent thread failures.")
    public void clearErrors() {
        EbslThreadErrorLog.clear();
    }
}
