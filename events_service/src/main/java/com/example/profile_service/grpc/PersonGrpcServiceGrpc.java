package com.example.profile_service.grpc;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 * <pre>
 * Вызывается events_service (VolunteerService — резолвит имена волонтёров по lichnostId).
 * </pre>
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.68.1)",
    comments = "Source: person.proto")
@io.grpc.stub.annotations.GrpcGenerated
public final class PersonGrpcServiceGrpc {

  private PersonGrpcServiceGrpc() {}

  public static final java.lang.String SERVICE_NAME = "profkom.profile.PersonGrpcService";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<com.example.profile_service.grpc.SearchPersonsRequest,
      com.example.profile_service.grpc.SearchPersonsResponse> getSearchPersonsMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "SearchPersons",
      requestType = com.example.profile_service.grpc.SearchPersonsRequest.class,
      responseType = com.example.profile_service.grpc.SearchPersonsResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.example.profile_service.grpc.SearchPersonsRequest,
      com.example.profile_service.grpc.SearchPersonsResponse> getSearchPersonsMethod() {
    io.grpc.MethodDescriptor<com.example.profile_service.grpc.SearchPersonsRequest, com.example.profile_service.grpc.SearchPersonsResponse> getSearchPersonsMethod;
    if ((getSearchPersonsMethod = PersonGrpcServiceGrpc.getSearchPersonsMethod) == null) {
      synchronized (PersonGrpcServiceGrpc.class) {
        if ((getSearchPersonsMethod = PersonGrpcServiceGrpc.getSearchPersonsMethod) == null) {
          PersonGrpcServiceGrpc.getSearchPersonsMethod = getSearchPersonsMethod =
              io.grpc.MethodDescriptor.<com.example.profile_service.grpc.SearchPersonsRequest, com.example.profile_service.grpc.SearchPersonsResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "SearchPersons"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.example.profile_service.grpc.SearchPersonsRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.example.profile_service.grpc.SearchPersonsResponse.getDefaultInstance()))
              .setSchemaDescriptor(new PersonGrpcServiceMethodDescriptorSupplier("SearchPersons"))
              .build();
        }
      }
    }
    return getSearchPersonsMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static PersonGrpcServiceStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<PersonGrpcServiceStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<PersonGrpcServiceStub>() {
        @java.lang.Override
        public PersonGrpcServiceStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new PersonGrpcServiceStub(channel, callOptions);
        }
      };
    return PersonGrpcServiceStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static PersonGrpcServiceBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<PersonGrpcServiceBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<PersonGrpcServiceBlockingStub>() {
        @java.lang.Override
        public PersonGrpcServiceBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new PersonGrpcServiceBlockingStub(channel, callOptions);
        }
      };
    return PersonGrpcServiceBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static PersonGrpcServiceFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<PersonGrpcServiceFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<PersonGrpcServiceFutureStub>() {
        @java.lang.Override
        public PersonGrpcServiceFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new PersonGrpcServiceFutureStub(channel, callOptions);
        }
      };
    return PersonGrpcServiceFutureStub.newStub(factory, channel);
  }

  /**
   * <pre>
   * Вызывается events_service (VolunteerService — резолвит имена волонтёров по lichnostId).
   * </pre>
   */
  public interface AsyncService {

    /**
     */
    default void searchPersons(com.example.profile_service.grpc.SearchPersonsRequest request,
        io.grpc.stub.StreamObserver<com.example.profile_service.grpc.SearchPersonsResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getSearchPersonsMethod(), responseObserver);
    }
  }

  /**
   * Base class for the server implementation of the service PersonGrpcService.
   * <pre>
   * Вызывается events_service (VolunteerService — резолвит имена волонтёров по lichnostId).
   * </pre>
   */
  public static abstract class PersonGrpcServiceImplBase
      implements io.grpc.BindableService, AsyncService {

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return PersonGrpcServiceGrpc.bindService(this);
    }
  }

  /**
   * A stub to allow clients to do asynchronous rpc calls to service PersonGrpcService.
   * <pre>
   * Вызывается events_service (VolunteerService — резолвит имена волонтёров по lichnostId).
   * </pre>
   */
  public static final class PersonGrpcServiceStub
      extends io.grpc.stub.AbstractAsyncStub<PersonGrpcServiceStub> {
    private PersonGrpcServiceStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected PersonGrpcServiceStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new PersonGrpcServiceStub(channel, callOptions);
    }

    /**
     */
    public void searchPersons(com.example.profile_service.grpc.SearchPersonsRequest request,
        io.grpc.stub.StreamObserver<com.example.profile_service.grpc.SearchPersonsResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getSearchPersonsMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   * A stub to allow clients to do synchronous rpc calls to service PersonGrpcService.
   * <pre>
   * Вызывается events_service (VolunteerService — резолвит имена волонтёров по lichnostId).
   * </pre>
   */
  public static final class PersonGrpcServiceBlockingStub
      extends io.grpc.stub.AbstractBlockingStub<PersonGrpcServiceBlockingStub> {
    private PersonGrpcServiceBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected PersonGrpcServiceBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new PersonGrpcServiceBlockingStub(channel, callOptions);
    }

    /**
     */
    public com.example.profile_service.grpc.SearchPersonsResponse searchPersons(com.example.profile_service.grpc.SearchPersonsRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getSearchPersonsMethod(), getCallOptions(), request);
    }
  }

  /**
   * A stub to allow clients to do ListenableFuture-style rpc calls to service PersonGrpcService.
   * <pre>
   * Вызывается events_service (VolunteerService — резолвит имена волонтёров по lichnostId).
   * </pre>
   */
  public static final class PersonGrpcServiceFutureStub
      extends io.grpc.stub.AbstractFutureStub<PersonGrpcServiceFutureStub> {
    private PersonGrpcServiceFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected PersonGrpcServiceFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new PersonGrpcServiceFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.example.profile_service.grpc.SearchPersonsResponse> searchPersons(
        com.example.profile_service.grpc.SearchPersonsRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getSearchPersonsMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_SEARCH_PERSONS = 0;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final AsyncService serviceImpl;
    private final int methodId;

    MethodHandlers(AsyncService serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_SEARCH_PERSONS:
          serviceImpl.searchPersons((com.example.profile_service.grpc.SearchPersonsRequest) request,
              (io.grpc.stub.StreamObserver<com.example.profile_service.grpc.SearchPersonsResponse>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }
  }

  public static final io.grpc.ServerServiceDefinition bindService(AsyncService service) {
    return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
        .addMethod(
          getSearchPersonsMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.example.profile_service.grpc.SearchPersonsRequest,
              com.example.profile_service.grpc.SearchPersonsResponse>(
                service, METHODID_SEARCH_PERSONS)))
        .build();
  }

  private static abstract class PersonGrpcServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    PersonGrpcServiceBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return com.example.profile_service.grpc.Person.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("PersonGrpcService");
    }
  }

  private static final class PersonGrpcServiceFileDescriptorSupplier
      extends PersonGrpcServiceBaseDescriptorSupplier {
    PersonGrpcServiceFileDescriptorSupplier() {}
  }

  private static final class PersonGrpcServiceMethodDescriptorSupplier
      extends PersonGrpcServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final java.lang.String methodName;

    PersonGrpcServiceMethodDescriptorSupplier(java.lang.String methodName) {
      this.methodName = methodName;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (PersonGrpcServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new PersonGrpcServiceFileDescriptorSupplier())
              .addMethod(getSearchPersonsMethod())
              .build();
        }
      }
    }
    return result;
  }
}
