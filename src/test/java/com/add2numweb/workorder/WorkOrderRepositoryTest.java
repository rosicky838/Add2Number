package com.add2numweb.workorder;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.JpaRepository;

class WorkOrderRepositoryTest {

    @Test
    void extendsJpaRepositoryWithWorkOrderAndStringId() {
        Type repositoryType = WorkOrderRepository.class.getGenericInterfaces()[0];

        assertThat(repositoryType).isInstanceOf(ParameterizedType.class);
        ParameterizedType parameterizedType = (ParameterizedType) repositoryType;
        assertThat(parameterizedType.getRawType()).isEqualTo(JpaRepository.class);
        assertThat(parameterizedType.getActualTypeArguments())
                .containsExactly(WorkOrder.class, String.class);
    }
}
