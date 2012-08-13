/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.esri.android.samples.bladerunner.laserrangefinder;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.esri.android.samples.bladerunner.BladeRunnerApplication;

/**
 * This class does all the work for setting up and managing Bluetooth
 * connections with other devices. It has a thread that listens for
 * incoming connections, a thread for connecting with a device, and a
 * thread for performing data transmissions when connected.
 * 
 * @version $Revision: 1.0 $
 */
public class BluetoothSerialService {
    private static final String TAG = BluetoothSerialService.class.getSimpleName();
    private static final boolean D = true;

//	private static final UUID SerialPortServiceClass_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // Member fields
    final BluetoothAdapter mAdapter;
    final Handler mHandler;
    ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    private int mState;
    
    ByteQueue mByteQueue;

    // Constants that indicate the current connection state
    public static final int STATE_STARTED = 0;       // we're doing nothing
    public static final int STATE_NONE = 1;       // we're doing nothing
    public static final int STATE_LISTEN = 2;     // now listening for incoming connections
    public static final int STATE_CONNECTING = 3; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 4;  // now connected to a remote device
    public static final int STATE_STOP = 5;  // now connected to a remote device

    /**
     * Constructor. Prepares a new BluetoothChat session.
     * @param context  The UI Activity Context
     * @param handler  A Handler to send messages back to the UI Activity
     * @param bq ByteQueue
     */
    public BluetoothSerialService(Context context, Handler handler, ByteQueue bq) {
    	mAdapter = BluetoothAdapter.getDefaultAdapter();
        mState = STATE_NONE;
        mHandler = handler;
        mByteQueue = bq;
    }

    /**
     * Set the current state of the chat connection
     * @param state  An integer defining the current connection state
     */
    private synchronized void setState(int state) {
        if (D) Log.d(TAG, "setState() " + mState + " -> " + state);
        mState = state;

        // Give the new state to the Handler so the UI Activity can update
        mHandler.obtainMessage(BladeRunnerApplication.BLUETOOTH_MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
    }
    
    /**
     * Method isConnecting.
    
     * @return boolean */
    public synchronized boolean isConnecting() {
    	return ((mConnectThread != null && mConnectThread.isAlive()) || (mConnectedThread != null && mConnectedThread.isAlive()));
    }

    /**
     * Return the current connection state. * @return int
     * @return int
     */
    public synchronized int getState() {
        return mState;
    }

    /**
     * Start the chat service. Specifically start AcceptThread to begin a
     * session in listening (server) mode. Called by the Activity onResume() */
    public synchronized void start() {
        // Cancel any thread attempting to make a connection
        if (mConnectThread != null) {
        	mConnectThread.cancel(); 
        	mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
        	mConnectedThread.cancel(); 
        	mConnectedThread = null;
        }

        setState(STATE_STARTED);
    }

    /**
     * Start the ConnectThread to initiate a connection to a remote device.
     * @param device  The BluetoothDevice to connect
     */
    public synchronized void connect(BluetoothDevice device) {
        // Cancel any thread attempting to make a connection
        if (mState == STATE_CONNECTING) {
            if (mConnectThread != null)
            	mConnectThread.cancel(); mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null)
        	mConnectedThread.cancel(); mConnectedThread = null;

        // Start the thread to connect with the given device
        mConnectThread = new ConnectThread(device);
        mConnectThread.start();
        setState(STATE_CONNECTING);
    }

    /**
     * Start the ConnectedThread to begin managing a Bluetooth connection
     * @param socket  The BluetoothSocket on which the connection was made
     * @param device  The BluetoothDevice that has been connected
     */
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
        // Cancel the thread that completed the connection
        if (mConnectThread != null) {
        	mConnectThread.cancel(); 
        	mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
        	mConnectedThread.cancel(); 
        	mConnectedThread = null;
        }

        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();

        // Send the name of the connected device back to the UI Activity
        Message msg = mHandler.obtainMessage(BladeRunnerApplication.BLUETOOTH_MESSAGE_DEVICE_NAME);
        Bundle bundle = new Bundle();
        bundle.putString(BladeRunnerApplication.BLUETOOTH_DEVICE_NAME, device.getName());
        msg.setData(bundle);
        mHandler.sendMessage(msg);

        setState(STATE_CONNECTED);
    }

    /**
     * Stop all threads
     */
    public synchronized void stop() {
        if (mConnectThread != null) {
        	mConnectThread.cancel(); 
        	mConnectThread = null;
        }

        if (mConnectedThread != null) {
        	mConnectedThread.cancel(); 
        	mConnectedThread = null;
        }

        setState(STATE_STOP);
    }

    /**
     * Write to the ConnectedThread in an unsynchronized manner
     * @param out The bytes to write
    
     * @see ConnectedThread#write(byte[]) */
    public void write(byte[] out) {
        ConnectedThread r;
        
        // Synchronize a copy of the ConnectedThread
        synchronized (this) {
            if (mState != STATE_CONNECTED) 
            	return;
            
            r = mConnectedThread;
        }
        // Perform the write unsynchronized
        r.write(out);
    }
    
    /**
     * Indicate that the connection attempt failed and notify the UI Activity.
     */
    void connectionFailed() {
        setState(STATE_NONE);

        // Send a failure message back to the Activity
        Message msg = mHandler.obtainMessage(BladeRunnerApplication.BLUETOOTH_MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(BladeRunnerApplication.BLUETOOTH_TOAST, "Unable to connect device");
        mHandler.sendMessage(msg);
    }

    /**
     * Indicate that the connection was lost and notify the UI Activity.
     */
    void connectionLost() {
        setState(STATE_NONE);

        // Send a failure message back to the Activity
        Message msg = mHandler.obtainMessage(BladeRunnerApplication.BLUETOOTH_MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(BladeRunnerApplication.BLUETOOTH_TOAST, "Device connection was lost");
        msg.setData(bundle);
        mHandler.sendMessage(msg);
    }

    /**
     * This thread runs while attempting to make an outgoing connection
     * with a device. It runs straight through; the connection either
     * succeeds or fails.
     * @author dan3488
     * @version $Revision: 1.0 $
     */
    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        /**
         * Constructor for ConnectThread.
         * @param device BluetoothDevice
         */
        public ConnectThread(BluetoothDevice device) {
            mmDevice = device;
            BluetoothSocket bluetoothSocket = null;

            // Get a BluetoothSocket for a connection with the given BluetoothDevice
            try {
            	Method m = mmDevice.getClass().getMethod("createRfcommSocket", new Class[] { int.class });
				bluetoothSocket = (BluetoothSocket) m.invoke(mmDevice, 1);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			}
            mmSocket = bluetoothSocket;
        }

        /**
         * Method run.
         * @see java.lang.Runnable#run()
         */
        @Override
        public void run() {
            setName("ConnectThread");

            // Always cancel discovery because it will slow down a connection
            mAdapter.cancelDiscovery();

            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                mmSocket.connect();
            } catch (IOException e) {
                connectionFailed();
                e.printStackTrace();
                // Close the socket
                try {
                    mmSocket.close();
                } catch (IOException e2) {
                    e2.printStackTrace();
                }
                return;
            }

            // Reset the ConnectThread because we're done
            synchronized (BluetoothSerialService.this) {
                mConnectThread = null;
            }

            // Start the connected thread
            connected(mmSocket, mmDevice);
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
            	e.printStackTrace();
            }
        }
    }

    /**
     * This thread runs during a connection with a remote device.
     * It handles all incoming and outgoing transmissions.
     * @author dan3488
     * @version $Revision: 1.0 $
     */
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        

        /**
         * Constructor for ConnectedThread.
         * @param socket BluetoothSocket
         */
        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            
            // Get the BluetoothSocket input and output streams
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
            	e.printStackTrace();
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        /**
         * Method run.
         * @see java.lang.Runnable#run()
         */
        @Override
        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;

            // Keep listening to the InputStream while connected
            while (true) {
                try {
                  // Read from the InputStream
                  bytes = mmInStream.read(buffer);
                   
            	    mByteQueue.write(buffer, 0, bytes);
                    
                    // Send the obtained bytes to the UI Activity
                    mHandler.sendMessage( mHandler.obtainMessage(BladeRunnerApplication.BLUETOOTH_MESSAGE_READ));
                } catch (InterruptedException e) {
                    //@TODO log the exception;
                  	break;
                } catch (IOException e) {
                    connectionLost();
                    break;
                }
            }
        }

        /**
         * Write to the connected OutStream.
         * @param buffer  The bytes to write
         */
        public void write(byte[] buffer) {
            try {
                mmOutStream.write(buffer);

                // Share the sent message back to the UI Activity
                mHandler.obtainMessage(BladeRunnerApplication.BLUETOOTH_MESSAGE_WRITE, buffer.length, -1, buffer).sendToTarget();
            } catch (IOException e) {
            	e.printStackTrace();
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
            	e.printStackTrace();
            }
        }
    }
}
