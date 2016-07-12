package com.technology.jep.jepcommon.security;

import java.security.Principal;

/**
 * TODO Перевести комментарии
 * <p> This class implements the <code>JepPrincipal</code> interface
 * and represents a Sample user.
 *
 * <p> Principals may be associated with a particular <code>Subject</code>
 * to augment that <code>Subject</code> with an additional
 * identity.  Refer to the <code>Subject</code> class for more information
 * on how to achieve this.  Authorization decisions can then be based upon 
 * the Principals associated with a <code>Subject</code>.
 * 
 * @see java.security.Principal
 * @see javax.security.auth.Subject
 */
public class JepPrincipal implements Principal {

  private String _name = null;
  private Integer _operatorId = null;

  public JepPrincipal(String role) {
    if (role == null) {
      throw new NullPointerException("name cannot be null");
    }
    this._name = role;
  }

  public JepPrincipal(String role, Integer operatorId) {
    if (role == null) {
      throw new NullPointerException("name cannot be null");
    }
    this._name = role;
    this._operatorId = operatorId;
  }

  /**
   * Return a string representation of this <code>JepPrincipal</code>.
   *
   * <p>
   *
   * @return a string representation of this <code>JepPrincipal</code>.
   */
  public String getName() {
    return _name;
  }

  /**
   * Возвращает иденитификатор опеартора размещенный в данный <code>JepPrincipal</code>.
   *
   * <p>
   *
   * @return иденитификатор опеартора размещенный в данный <code>JepPrincipal</code>.
   */
  public Integer getOperatorId() {
    return _operatorId;
  }

  /**
   * Return a string representation of this <code>JepPrincipal</code>.
   *
   * <p>
   *
   * @return a string representation of this <code>JepPrincipal</code>.
   */
  public String toString() {
    return "[JepPrincipal] : " + _name;
  }

  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_name == null) ? 0 : _name.hashCode());
    result = prime * result
        + ((_operatorId == null) ? 0 : _operatorId.hashCode());
    return result;
  }

  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    JepPrincipal other = (JepPrincipal) obj;
    if (_name == null) {
      if (other._name != null)
        return false;
    } else if (!_name.equals(other._name))
      return false;
    if (_operatorId == null) {
      if (other._operatorId != null)
        return false;
    } else if (!_operatorId.equals(other._operatorId))
      return false;
    return true;
  }
}
