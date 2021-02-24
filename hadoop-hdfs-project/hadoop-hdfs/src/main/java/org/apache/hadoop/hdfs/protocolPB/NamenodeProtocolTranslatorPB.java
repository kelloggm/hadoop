/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.hadoop.hdfs.protocolPB;

import org.apache.hadoop.classification.InterfaceAudience;
import org.apache.hadoop.classification.InterfaceStability;
import org.apache.hadoop.hdfs.protocol.DatanodeID;
import org.apache.hadoop.hdfs.protocol.DatanodeInfo;
import org.apache.hadoop.hdfs.protocol.proto.HdfsServerProtos.NamenodeCommandProto;
import org.apache.hadoop.hdfs.protocol.proto.HdfsServerProtos.VersionRequestProto;
import org.apache.hadoop.hdfs.protocol.proto.NamenodeProtocolProtos.*;
import org.apache.hadoop.hdfs.security.token.block.ExportedBlockKeys;
import org.apache.hadoop.hdfs.server.namenode.CheckpointSignature;
import org.apache.hadoop.hdfs.server.protocol.*;
import org.apache.hadoop.ipc.*;
import org.apache.hadoop.thirdparty.protobuf.RpcController;
import org.apache.hadoop.thirdparty.protobuf.ServiceException;
import org.checkerframework.checker.mustcall.qual.InheritableMustCall;

import java.io.Closeable;
import java.io.IOException;

/**
 * This class is the client side translator to translate the requests made on
 * {@link NamenodeProtocol} interfaces to the RPC server implementing
 * {@link NamenodeProtocolPB}.
 */
@InterfaceAudience.Private
@InterfaceStability.Stable
@InheritableMustCall({})
public class NamenodeProtocolTranslatorPB implements NamenodeProtocol,
    ProtocolMetaInterface, Closeable, ProtocolTranslator {
  /** RpcController is not used and hence is set to null */
  private final static RpcController NULL_CONTROLLER = null;
  
  /*
   * Protobuf requests with no parameters instantiated only once
   */
  private static final GetBlockKeysRequestProto VOID_GET_BLOCKKEYS_REQUEST = 
      GetBlockKeysRequestProto.newBuilder().build();
  private static final GetTransactionIdRequestProto VOID_GET_TRANSACTIONID_REQUEST = 
      GetTransactionIdRequestProto.newBuilder().build();
  private static final RollEditLogRequestProto VOID_ROLL_EDITLOG_REQUEST = 
      RollEditLogRequestProto.newBuilder().build();
  private static final VersionRequestProto VOID_VERSION_REQUEST = 
      VersionRequestProto.newBuilder().build();

  final private NamenodeProtocolPB rpcProxy;
  
  public NamenodeProtocolTranslatorPB(NamenodeProtocolPB rpcProxy) {
    this.rpcProxy = rpcProxy;
  }

  @Override
  public void close() {
    RPC.stopProxy(rpcProxy);
  }

  @Override
  public Object getUnderlyingProxyObject() {
    return rpcProxy;
  }

  @Override
  public BlocksWithLocations getBlocks(DatanodeInfo datanode, long size, long
      minBlockSize)
      throws IOException {
    GetBlocksRequestProto req = GetBlocksRequestProto.newBuilder()
        .setDatanode(PBHelperClient.convert((DatanodeID)datanode)).setSize(size)
        .setMinBlockSize(minBlockSize).build();
    try {
      return PBHelper.convert(rpcProxy.getBlocks(NULL_CONTROLLER, req)
          .getBlocks());
    } catch (ServiceException e) {
      throw ProtobufHelper.getRemoteException(e);
    }
  }

  @Override
  public ExportedBlockKeys getBlockKeys() throws IOException {
    try {
      GetBlockKeysResponseProto rsp = rpcProxy.getBlockKeys(NULL_CONTROLLER,
          VOID_GET_BLOCKKEYS_REQUEST);
      return rsp.hasKeys() ? PBHelper.convert(rsp.getKeys()) : null;
    } catch (ServiceException e) {
      throw ProtobufHelper.getRemoteException(e);
    }
  }

  @Override
  public long getTransactionID() throws IOException {
    try {
      return rpcProxy.getTransactionId(NULL_CONTROLLER,
          VOID_GET_TRANSACTIONID_REQUEST).getTxId();
    } catch (ServiceException e) {
      throw ProtobufHelper.getRemoteException(e);
    }
  }

  @Override
  public long getMostRecentCheckpointTxId() throws IOException {
    try {
      return rpcProxy.getMostRecentCheckpointTxId(NULL_CONTROLLER,
          GetMostRecentCheckpointTxIdRequestProto.getDefaultInstance()).getTxId();
    } catch (ServiceException e) {
      throw ProtobufHelper.getRemoteException(e);
    }
  }

  @Override
  public CheckpointSignature rollEditLog() throws IOException {
    try {
      return PBHelper.convert(rpcProxy.rollEditLog(NULL_CONTROLLER,
          VOID_ROLL_EDITLOG_REQUEST).getSignature());
    } catch (ServiceException e) {
      throw ProtobufHelper.getRemoteException(e);
    }
  }

  @Override
  public NamespaceInfo versionRequest() throws IOException {
    try {
      return PBHelper.convert(rpcProxy.versionRequest(NULL_CONTROLLER,
          VOID_VERSION_REQUEST).getInfo());
    } catch (ServiceException e) {
      throw ProtobufHelper.getRemoteException(e);
    }
  }

  @Override
  public void errorReport(NamenodeRegistration registration, int errorCode,
      String msg) throws IOException {
    ErrorReportRequestProto req = ErrorReportRequestProto.newBuilder()
        .setErrorCode(errorCode).setMsg(msg)
        .setRegistration(PBHelper.convert(registration)).build();
    try {
      rpcProxy.errorReport(NULL_CONTROLLER, req);
    } catch (ServiceException e) {
      throw ProtobufHelper.getRemoteException(e);
    }
  }

  @Override
  public NamenodeRegistration registerSubordinateNamenode(
      NamenodeRegistration registration) throws IOException {
    RegisterRequestProto req = RegisterRequestProto.newBuilder()
        .setRegistration(PBHelper.convert(registration)).build();
    try {
      return PBHelper.convert(
          rpcProxy.registerSubordinateNamenode(NULL_CONTROLLER, req)
          .getRegistration());
    } catch (ServiceException e) {
      throw ProtobufHelper.getRemoteException(e);
    }
  }

  @Override
  public NamenodeCommand startCheckpoint(NamenodeRegistration registration)
      throws IOException {
    StartCheckpointRequestProto req = StartCheckpointRequestProto.newBuilder()
        .setRegistration(PBHelper.convert(registration)).build();
    NamenodeCommandProto cmd;
    try {
      cmd = rpcProxy.startCheckpoint(NULL_CONTROLLER, req).getCommand();
    } catch (ServiceException e) {
      throw ProtobufHelper.getRemoteException(e);
    }
    return PBHelper.convert(cmd);
  }

  @Override
  public void endCheckpoint(NamenodeRegistration registration,
      CheckpointSignature sig) throws IOException {
    EndCheckpointRequestProto req = EndCheckpointRequestProto.newBuilder()
        .setRegistration(PBHelper.convert(registration))
        .setSignature(PBHelper.convert(sig)).build();
    try {
      rpcProxy.endCheckpoint(NULL_CONTROLLER, req);
    } catch (ServiceException e) {
      throw ProtobufHelper.getRemoteException(e);
    }
  }

  @Override
  public RemoteEditLogManifest getEditLogManifest(long sinceTxId)
      throws IOException {
    GetEditLogManifestRequestProto req = GetEditLogManifestRequestProto
        .newBuilder().setSinceTxId(sinceTxId).build();
    try {
      return PBHelper.convert(rpcProxy.getEditLogManifest(NULL_CONTROLLER, req)
          .getManifest());
    } catch (ServiceException e) {
      throw ProtobufHelper.getRemoteException(e);
    }
  }

  @Override
  public boolean isMethodSupported(String methodName) throws IOException {
    return RpcClientUtil.isMethodSupported(rpcProxy, NamenodeProtocolPB.class,
        RPC.RpcKind.RPC_PROTOCOL_BUFFER,
        RPC.getProtocolVersion(NamenodeProtocolPB.class), methodName);
  }

  @Override
  public boolean isUpgradeFinalized() throws IOException {
    IsUpgradeFinalizedRequestProto req = IsUpgradeFinalizedRequestProto
        .newBuilder().build();
    try {
      IsUpgradeFinalizedResponseProto response = rpcProxy.isUpgradeFinalized(
          NULL_CONTROLLER, req);
      return response.getIsUpgradeFinalized();
    } catch (ServiceException e) {
      throw ProtobufHelper.getRemoteException(e);
    }
  }

  @Override
  public boolean isRollingUpgrade() throws IOException {
    IsRollingUpgradeRequestProto req = IsRollingUpgradeRequestProto
        .newBuilder().build();
    try {
      IsRollingUpgradeResponseProto response = rpcProxy.isRollingUpgrade(
          NULL_CONTROLLER, req);
      return response.getIsRollingUpgrade();
    } catch (ServiceException e) {
      throw ProtobufHelper.getRemoteException(e);
    }
  }

  @Override
  public Long getNextSPSPath() throws IOException {
    GetNextSPSPathRequestProto req =
        GetNextSPSPathRequestProto.newBuilder().build();
    try {
      GetNextSPSPathResponseProto nextSPSPath =
          rpcProxy.getNextSPSPath(NULL_CONTROLLER, req);
      return nextSPSPath.hasSpsPath() ? nextSPSPath.getSpsPath() : null;
    } catch (ServiceException e) {
      throw ProtobufHelper.getRemoteException(e);
    }
  }
}
