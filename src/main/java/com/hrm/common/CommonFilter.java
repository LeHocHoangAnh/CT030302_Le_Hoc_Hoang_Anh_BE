package com.hrm.common;

public class CommonFilter {

    public enum BOOKING_DAY_OFF {
        // 0 - 1 - 1 - 2 - 3 - 4 - 5 - 6
        DAY_OFF, WORKING_EARLY,WORKING_LATE, REMOTE, GO_OUT,OT,PERSONAL_LEAVE,COMPENSATORY_LEAVE, UNPAID_LEAVE, KEEPING_FORGET, WORKUP_FOR_LATE
    }
    public enum Booking_OFF_TYPE{
    	DAY_OFF(0), OT(4), COMPENSATORY_LEAVE(6);
    	private int value;
    	Booking_OFF_TYPE(int value) {
            this.value = value;
        }
        public int getValue() {
            return value;
        }
    }
    public enum TYPE_CONTRACT{
    	OFFICIAL_CONTRACT(1), PROBATIONARY_CONTRACT(2), INTERNSHIP_CONTRACT(4), FREELANCE_CONTRACT(3);
    	private int value;
    	TYPE_CONTRACT(int value) {
            this.value = value;
        }
        public int getValue() {
            return value;
        }
    }
    
    public enum ROLES {
        // 0 - 1 - 1 - 2 - 3 - 4 - 5 - 6
        LEADER, SUB_LEADER, HR, COMTOR, CUSTOMER
    }
}
