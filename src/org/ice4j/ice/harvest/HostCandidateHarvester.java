/*
 * ice4j, the OpenSource Java Solution for NAT and Firewall Traversal.
 * Maintained by the SIP Communicator community (http://sip-communicator.org).
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.ice4j.ice.harvest;

import java.io.*;
import java.net.*;

import net.java.sip.communicator.impl.netaddr.*;
import net.java.sip.communicator.service.configuration.*;
import net.java.sip.communicator.util.*;

import org.ice4j.ice.*;

/**
 * A <tt>HostCandidateHarvester</tt> gathers host <tt>Candidate</tt>s for a
 * specified {@link org.ice4j.ice.Component}. Most <tt>CandidateHarvester</tt>s
 * would rely on the output of the host harvester, that is all host addresses,
 * to be already present and bound in a <tt>Component</tt> before being able to
 * harvest the type of addresses that they are responsible for.
 *
 * @author Emil Ivov
 */
public class HostCandidateHarvester
{

    /**
     * Gathers all candidate addresses on the local machine, binds sockets on
     * them and creates {@link HostCandidate}s. The harvester would always
     * try to bind the sockets on the specified <tt>preferredPort</tt> first.
     * If that fails we will move through all ports between <tt>minPort</tt> and
     * <tt>maxPort</tt> and give up if still can't find a free port.
     *
     * @param component the {@link Component} that we'd like to gather candidate
     * addresses for.
     * @param preferredPort the port number that should be tried first when
     * binding local <tt>Candidate</tt> sockets for this <tt>Component</tt>.
     * @param minPort the port number where we should first try to bind before
     * moving to the next one (i.e. <tt>minPort + 1</tt>)
     * @param maxPort the maximum port number where we should try binding
     * before giving up and throwinG an exception.
     *
     * @throws IllegalArgumentException if either <tt>minPort</tt> or
     * <tt>maxPort</tt> is not a valid port number or if <tt>minPort >
     * maxPort</tt>.
     * @throws IOException if an error occurs while the underlying resolver lib
     * is using sockets.
     * @throws BindException if we couldn't find a free port between
     * <tt>minPort</tt> and <tt>maxPort</tt> before reaching the maximum allowed
     * number of retries.
     */
    public void harvest(Component component,
                        int       preferredPort,
                        int       minPort,
                        int       maxPort)
        throws IllegalArgumentException,
               IOException,
               BindException
    {

    }

    /**
     * Creates a <tt>DatagramSocket</tt> and binds it to the specified
     * <tt>localAddress</tt> and a port in the range specified by the
     * <tt>minPort</tt> and <tt>maxPort</tt> parameters. We first try to bind
     * the newly created socket on the <tt>preferredPort</tt> port number
     * (unless it is outside the <tt>[minPort, maxPort]</tt> range in which case
     * we first try the <tt>minPort</tt>) and then proceed incrementally upwards
     * until we succeed or reach the bind retries limit. If we reach the
     * <tt>maxPort</tt> port number before the bind retries limit, we will then
     * start over again at <tt>minPort</tt> and keep going until we run out of
     * retries.
     *
     * @param laddr the address that we'd like to bind the socket on.
     * @param preferredPort the port number that we should try to bind to first.
     * @param minPort the port number where we should first try to bind before
     * moving to the next one (i.e. <tt>minPort + 1</tt>)
     * @param maxPort the maximum port number where we should try binding
     * before giving up and throwinG an exception.
     *
     * @return the newly created <tt>DatagramSocket</tt>.
     *
     * @throws IllegalArgumentException if either <tt>minPort</tt> or
     * <tt>maxPort</tt> is not a valid port number or if <tt>minPort >
     * maxPort</tt>.
     * @throws IOException if an error occurs while the underlying resolver lib
     * is using sockets.
     * @throws BindException if we couldn't find a free port between
     * <tt>minPort</tt> and <tt>maxPort</tt> before reaching the maximum allowed
     * number of retries.
     */
    public DatagramSocket createDatagramSocket(InetAddress laddr,
                                               int preferredPort,
                                               int minPort,
                                               int maxPort)
        throws IllegalArgumentException,
               IOException,
               BindException
    {
        // make sure port numbers are valid
        if (!NetworkUtils.isValidPortNumber(minPort)
                        || !NetworkUtils.isValidPortNumber(maxPort))
        {
            throw new IllegalArgumentException("minPort (" + minPort
                            + ") and maxPort (" + maxPort + ") "
                            + "should be integers between 1024 and 65535.");
        }

        // make sure minPort comes before maxPort.
        if (minPort > maxPort)
        {
            throw new IllegalArgumentException("minPort (" + minPort
                            + ") should be less than or "
                            + "equal to maxPort (" + maxPort + ")");
        }

        // if preferredPort is not  in the allowed range, place it at min.
        if (minPort > preferredPort || preferredPort > maxPort)
        {
            throw new IllegalArgumentException("preferredPort ("+preferredPort
                            +") must be between minPort (" + minPort
                            + ") and maxPort (" + maxPort + ")");
        }

        int bindRetries = config.getInt(BIND_RETRIES_PROPERTY_NAME,
                        BIND_RETRIES_DEFAULT_VALUE);

        int port = preferredPort;
        for (int i = 0; i < bindRetries; i++)
        {

            try
            {
                return new DatagramSocket(port, laddr);
            }
            catch (SocketException se)
            {
                logger.info(
                    "Retrying a bind because of a failure to bind to address "
                        + laddr + " and port " + port, se);
            }

            port ++;

            if (port > maxPort)
                port = minPort;
        }

        throw new BindException("Could not bind to any port between "
                        + minPort + " and " + (port -1));
    }
}