package de.domschmidt.koku.transformer;

import de.domschmidt.koku.dto.activity.ActivitySequenceItemDto;
import de.domschmidt.koku.dto.activity.ActivityStepDto;
import de.domschmidt.koku.dto.customer.CustomerAppointmentActivityDto;
import de.domschmidt.koku.dto.customer.CustomerAppointmentDto;
import de.domschmidt.koku.dto.customer.CustomerAppointmentSoldProductDto;
import de.domschmidt.koku.dto.customer.CustomerDto;
import de.domschmidt.koku.dto.product.ProductDto;
import de.domschmidt.koku.dto.user.KokuUserDetailsDto;
import de.domschmidt.koku.persistence.model.*;
import de.domschmidt.koku.persistence.model.auth.KokuUser;
import de.domschmidt.koku.transformer.common.ITransformer;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class CustomerAppointmentToCustomerAppointmentDtoTransformer implements ITransformer<CustomerAppointment, CustomerAppointmentDto> {

    public List<CustomerAppointmentDto> transformToDtoList(final List<CustomerAppointment> modelList) {
        return transformToDtoList(modelList, true);
    }

    public List<CustomerAppointmentDto> transformToDtoList(final List<CustomerAppointment> modelList, final boolean detailed) {
        final List<CustomerAppointmentDto> result = new ArrayList<>();
        for (final CustomerAppointment customerAppointment : modelList) {
            result.add(transformToDto(customerAppointment, detailed));
        }
        return result;
    }

    public CustomerAppointmentDto transformToDto(final CustomerAppointment model) {
        return transformToDto(model, true);
    }

    public CustomerAppointmentDto transformToDto(final CustomerAppointment model, final boolean detailed) {
        final CustomerAppointmentDto.CustomerAppointmentDtoBuilder customerAppointmentDtoBuilder
                = CustomerAppointmentDto.builder()
                .id(model.getId())
                .description(model.getDescription())
                .startDate(model.getStart().toLocalDate())
                .startTime(model.getStart().toLocalTime())
                .user(KokuUserDetailsDto.builder()
                        .id(model.getUser().getId())
                        .firstname(model.getUser().getUserDetails().getFirstname())
                        .lastname(model.getUser().getUserDetails().getLastname())
                        .avatarBase64(model.getUser().getUserDetails().getAvatarBase64())
                        .build())
                .additionalInfo(model.getAdditionalInfo());
        if (detailed) {
            final CustomerDto customer;
            if (model.getCustomer() != null) {
                customer = new CustomerToCustomerDtoTransformer().transformToDto(model.getCustomer());
            } else {
                customer = null;
            }
            customerAppointmentDtoBuilder
                    .customer(customer)
                    .activities(new CustomerAppointmentActivityToCustomerAppointmentActivityDtoTransformer().transformToDtoList(model.getActivities()))
                    .soldProducts(new CustomerAppointmentSoldProductToCustomerAppointmentSoldProductDtoTransformer().transformToDtoList(model.getSoldProducts()))
                    .approximatelyDuration(calculateApproximatelyDurationOfAllActivities(model.getActivities()))
                    .activitySequenceItems(transformActivitySequenceItems(model.getActivitySequenceItems()))
                    .promotions(new PromotionToPromotionDtoTransformer().transformToDtoList(model.getPromotions()))
                    .build();
        }
        return customerAppointmentDtoBuilder.build();
    }

    private Duration calculateApproximatelyDurationOfAllActivities(List<CustomerAppointmentActivity> activities) {
        Duration duration = Duration.ZERO;
        if (activities != null) {
            for (final CustomerAppointmentActivity currentActivity : activities) {
                if (currentActivity.getActivity().getApproximatelyDuration() != null) {
                    duration = duration.plus(currentActivity.getActivity().getApproximatelyDuration());
                }
            }
        }
        return duration;
    }

    public CustomerAppointment transformToEntity(final CustomerAppointmentDto dtoModel) {
        final Customer customer;
        if (dtoModel.getCustomer() != null) {
            customer = new CustomerToCustomerDtoTransformer().transformToEntity(dtoModel.getCustomer());
        } else {
            customer = null;
        }

        final CustomerAppointment customerAppointment = CustomerAppointment.builder()
                .id(dtoModel.getId())
                .description(dtoModel.getDescription())
                .start(dtoModel.getStartDate().atTime(dtoModel.getStartTime()))
                .customer(customer)
                .additionalInfo(dtoModel.getAdditionalInfo())
                .user(KokuUser.builder()
                        .id(dtoModel.getUser().getId())
                        .build())
                .build();
        customerAppointment.setSoldProducts(transformSoldProducts(dtoModel, customerAppointment));
        customerAppointment.setActivities(transformActivities(dtoModel, customerAppointment));
        customerAppointment.setActivitySequenceItems(transformActivitySequenceItems(dtoModel, customerAppointment));
        customerAppointment.setPromotions(new PromotionToPromotionDtoTransformer().transformToEntityList(dtoModel.getPromotions()));

        return customerAppointment;
    }

    private List<CustomerAppointmentSoldProduct> transformSoldProducts(CustomerAppointmentDto model,
                                                                       CustomerAppointment customerAppointment) {
        final List<CustomerAppointmentSoldProduct> result = new ArrayList<>();

        if (model.getSoldProducts() != null && !model.getSoldProducts().isEmpty()) {
            List<CustomerAppointmentSoldProductDto> soldProducts = model.getSoldProducts();
            for (int soldProductIndex = 0, soldProductsSize = soldProducts.size(); soldProductIndex < soldProductsSize; soldProductIndex++) {
                CustomerAppointmentSoldProductDto customerAppointmentSoldProductDto = soldProducts.get(soldProductIndex);
                result.add(CustomerAppointmentSoldProduct.builder()
                        .id(customerAppointmentSoldProductDto.getId())
                        .product(new ProductToProductDtoTransformer().transformToEntity(customerAppointmentSoldProductDto.getProduct()))
                        .customerAppointment(customerAppointment)
                        .position(soldProductIndex)
                        .sellPrice(customerAppointmentSoldProductDto.getSellPrice())
                        .build());
            }
        }

        return result;
    }

    private List<CustomerAppointmentActivity> transformActivities(CustomerAppointmentDto model,
                                                                     CustomerAppointment customerAppointment) {
        final List<CustomerAppointmentActivity> result = new ArrayList<>();

        if (model.getActivities() != null && !model.getActivities().isEmpty()) {
            List<CustomerAppointmentActivityDto> activities = model.getActivities();
            for (int activityIndex = 0, activitiesSize = activities.size(); activityIndex < activitiesSize; activityIndex++) {
                CustomerAppointmentActivityDto customerAppointmentActivityDto = activities.get(activityIndex);
                result.add(CustomerAppointmentActivity.builder()
                        .id(customerAppointmentActivityDto.getId())
                        .activity(new ActivityToActivityDtoTransformer().transformToEntity(customerAppointmentActivityDto.getActivity()))
                        .customerAppointment(customerAppointment)
                        .position(activityIndex)
                        .sellPrice(customerAppointmentActivityDto.getSellPrice())
                        .build());
            }
        }

        return result;
    }

    private List<ActivitySequenceItemDto> transformActivitySequenceItems(final List<ActivitySequenceItem> sequenceItems) {
        final List<ActivitySequenceItemDto> result = new ArrayList<>();

        if (sequenceItems != null && !sequenceItems.isEmpty()) {
            for (final ActivitySequenceItem activitySequenceItem : sequenceItems) {
                if (activitySequenceItem.getOptionalActivityStep() != null) {
                    final ActivityStepDto activityStepDto = new ActivityStepToActivityStepDtoTransformer().transformToDto(activitySequenceItem.getOptionalActivityStep());
                    activityStepDto.setSequenceId(activitySequenceItem.getId());
                    result.add(activityStepDto);
                } else if (activitySequenceItem.getOptionalProduct() != null) {
                    final ProductDto productDto = new ProductToProductDtoTransformer().transformToDto(activitySequenceItem.getOptionalProduct());
                    productDto.setSequenceId(activitySequenceItem.getId());
                    result.add(productDto);
                }
            }
        }

        return result;
    }

    private List<ActivitySequenceItem> transformActivitySequenceItems(final CustomerAppointmentDto model, CustomerAppointment customerAppointment) {
        final List<ActivitySequenceItem> result = new ArrayList<>();

        if (model.getActivitySequenceItems() != null && !model.getActivitySequenceItems().isEmpty()) {
            List<ActivitySequenceItemDto> activitySequenceItems = model.getActivitySequenceItems();
            for (int activitySequenceIndex = 0; activitySequenceIndex < activitySequenceItems.size(); activitySequenceIndex++) {
                ActivitySequenceItemDto activitySequenceItem = activitySequenceItems.get(activitySequenceIndex);
                if (activitySequenceItem instanceof ActivityStepDto) {
                    result.add(ActivitySequenceItem.builder()
                            .id(activitySequenceItem.getSequenceId())
                            .optionalActivityStep(new ActivityStepToActivityStepDtoTransformer().transformToEntity((ActivityStepDto) activitySequenceItem))
                            .customerAppointment(customerAppointment)
                            .position(activitySequenceIndex)
                            .build());
                } else if (activitySequenceItem instanceof ProductDto) {
                    result.add(ActivitySequenceItem.builder()
                            .id(activitySequenceItem.getSequenceId())
                            .optionalProduct(new ProductToProductDtoTransformer().transformToEntity((ProductDto) activitySequenceItem))
                            .customerAppointment(customerAppointment)
                            .position(activitySequenceIndex)
                            .build());
                }
            }
        }

        return result;
    }
}
