package com.enterprises_management.enterprise.application.ports.output;

import com.enterprises_management.enterprise.domain.models.Enterprise;

public interface IEnterpriseCreateOutputPort {
        /**
         * Create a new enterprise
         * @param enterprise
         * @return
         */
        public Enterprise create(Enterprise enterprise);
}
