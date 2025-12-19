import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

class Scratch {
  public static void main(String[] args) {
    String head = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
        "<model version=\"7.0.0\" name=\"test-02\">\n";
    StringBuilder pointValue = new StringBuilder();
    StringBuilder vehicleValue = new StringBuilder();
    StringBuilder locationValue = new StringBuilder();
    StringBuilder pathValue = new StringBuilder();
    String locationTypeValue = "<locationType name=\"Location\">\n" +
        "        <allowedOperation name=\"Load Cargo\"/>\n" +
        "        <allowedOperation name=\"NOP\"/>\n" +
        "        <allowedOperation name=\"Unload Cargo\"/>\n" +
        "        <locationTypeLayout locationRepresentation=\"LOAD_TRANSFER_GENERIC\"/>\n" +
        "    </locationType>\n" +
        "    <locationType name=\"Lift\">\n" +
        "        <allowedOperation name=\"Load Cargo\"/>\n" +
        "        <locationTypeLayout locationRepresentation=\"WORKING_GENERIC\"/>\n" +
        "    </locationType>\n" +
        "    <locationType name=\"Transfer Point\">\n" +
        "        <allowedOperation name=\"Load Cargo\"/>\n" +
        "        <locationTypeLayout locationRepresentation=\"WORKING_GENERIC\"/>\n" +
        "    </locationType>\n" +
        "    <locationType name=\"Recharge\">\n" +
        "        <allowedOperation name=\"NOP\"/>\n" +
        "        <allowedOperation name=\"Recharge\"/>\n" +
        "        <locationTypeLayout locationRepresentation=\"RECHARGE_GENERIC\"/>\n" +
        "    </locationType>\n";
    String end =  "    <property name=\"tcs:modelFileLastModified\" value=\"2025-12-09T01:32:56Z\"/>\n" +
        "</model>\n";
    int x = 18;
    int y = 16;
    int z = 2;
    //针对于失效库位的部分，临时修改
    Map<Integer, String[]> vehicleTotalityMap = new HashMap<>();
    vehicleTotalityMap.put(1, new String[]{"010101_P", "#FF0000"});
    vehicleTotalityMap.put(2, new String[]{"030101_P", "#00CCFF"});
    vehicleTotalityMap.put(3, new String[]{"050101_P", "#33CC00"});
    vehicleTotalityMap.put(4, new String[]{"070101_P", "#CC00FF"});

    for (Map.Entry<Integer, String[]> integerStringEntry : vehicleTotalityMap.entrySet()) {
      vehicleValue.append("<vehicle name=\"" + integerStringEntry.getKey() + "\" energyLevelCritical=\"30\" energyLevelGood=\"70\" energyLevelFullyRecharged=\"95\" energyLevelSufficientlyRecharged=\"50\" maxVelocity=\"1000\" maxReverseVelocity=\"1000\" envelopeKey=\"\">\n" +
          "        <boundingBox length=\"1000\" width=\"1000\" height=\"1000\" referenceOffsetX=\"0\" referenceOffsetY=\"0\"/>\n" +
          "        <property name=\"loopback:acceleration\" value=\"300\"/>\n" +
          "        <property name=\"loopback:deceleration\" value=\"300\"/>\n" +
          "        <property name=\"loopback:loadOperation\" value=\"Load Cargo\"/>\n" +
          "        <property name=\"tcs:rechargePriority\" value=\"normal\"/>\n" +
          "        <property name=\"loopback:unloadOperation\" value=\"Unload Cargo\"/>\n" +
          "        <property name=\"loopback:initialPosition\" value=\"" + integerStringEntry.getValue()[0] + "\"/>\n" +
          "        <vehicleLayout color=\"" + integerStringEntry.getValue()[1] + "\"/>\n" +
          "    </vehicle>\n");
    }
    int chargeNum = 0;
    int liftNum = 0;
    Map<String, Integer> liftNumMap = new HashMap<>();
    Map<String, String> locToCharge = new HashMap<>();
    Map<String, String> locToLift = new HashMap<>();
    Map<String, String> locToMap = new HashMap<>();
    Map<String, List<String>> tpBlockMember = new HashMap<>();
    Map<String, List<String>> liftBlockMember = new HashMap<>();
    for (int xx = 1; xx <= x; xx++) {
      for (int yy = 1; yy <= y; yy++) {
        for (int zz = 1; zz <= z; zz++) {
          String locationType = "Location";
          //跳过的条件,需要动态变化
          if ((xx >= 8 && yy >= 11) && !(xx == 8 && (yy == 12 || yy == 15))) {
            continue;
          }
          String locationCode = String.format("%02d", xx) + String.format("%02d", yy) + String.format("%02d", zz);
          int positionPointX = yy * 1500 + ((yy - 1) * 3750) + 750;
          int positionPointY = (x - xx + 1) * 1500 + ((x - xx) * 3750) - 1500 + ((zz - 1) * 100000);
          StringBuilder point = new StringBuilder();
          if ((zz >= 2 && xx == 7 && yy == 14) || (zz == 2 && xx == 7 && yy == 13)) {
            String chargeCode = locToCharge.get(locationCode);
            String locationCodeAbove = String.format("%02d", xx - 1) + String.format("%02d", yy) + String.format("%02d", zz) + "_P";
            point.append("<point name=\"CHARGE_POINT_" + chargeCode +"\" type=\"PARK_POSITION\" positionX=\"" + positionPointX + "\" positionY=\"" + positionPointY + "\" positionZ=\"0\" vehicleOrientationAngle=\"NaN\">\n"
                + "        <maxVehicleBoundingBox length=\"1200\" width=\"1200\" height=\"1000\" referenceOffsetX=\"0\" referenceOffsetY=\"0\"/>\n"
                + "        <outgoingPath name=\"CHARGE_POINT_" + chargeCode + " --- " + locationCodeAbove + "\"/>\n"
                + "        <property name=\"tcs:chargingPoint\" value=\"true\"/>\n"
                + "        <pointLayout labelOffsetX=\"-10\" labelOffsetY=\"-20\" layerId=\"" + (zz - 1) + "\"/>\n"
                + "</point>\n");
            pathValue.append("<path name=\"CHARGE_POINT_" + chargeCode + " --- " + locationCodeAbove + "\" sourcePoint=\"CHARGE_POINT_" + chargeCode + "\" destinationPoint=\"" + locationCodeAbove + "\" length=\"1300\" maxVelocity=\"10000\" maxReverseVelocity=\"0\" locked=\"false\">\n" +
                "        <pathLayout connectionType=\"DIRECT\" layerId=\"" + (zz - 1) + "\"/>\n" +
                "    </path>\n");
          } else if (xx == 7 && (yy == 12 || yy == 15)) {
            String tpCode = locToLift.get(String.format("%02d", xx + 1) + String.format("%02d", yy));
            String tpCode2 = String.format("%02d", xx + 1) + String.format("%02d", yy) + String.format("%02d", zz);
            String tpName = "TP_" + "L" + zz  + "_" + tpCode + "_POINT";
            tpBlockMember.computeIfAbsent(tpCode2, k -> new ArrayList<>()).add(tpName);
            String locationCodeAbove = String.format("%02d", xx - 1) + String.format("%02d", yy) + String.format("%02d", zz) + "_P";
            String locationCodeBelow = locToMap.get(String.format("%02d", xx + 1) + String.format("%02d", yy) + String.format("%02d", zz) + "_P");
            tpBlockMember.computeIfAbsent(tpCode2, k -> new ArrayList<>()).add(locationCodeAbove);
            tpBlockMember.computeIfAbsent(tpCode2, k -> new ArrayList<>()).add(locationCodeBelow);
            point.append("<point name=\"" + tpName +"\" type=\"HALT_POSITION\" positionX=\"" + positionPointX + "\" positionY=\"" + positionPointY + "\" positionZ=\"0\" vehicleOrientationAngle=\"NaN\">\n"
                + "        <maxVehicleBoundingBox length=\"1000\" width=\"1000\" height=\"1000\" referenceOffsetX=\"0\" referenceOffsetY=\"0\"/>\n"
                + "        <outgoingPath name=\"" + tpName + " --- " + locationCodeAbove + "\"/>\n"
                + "        <outgoingPath name=\"" + tpName + " --- " + locationCodeBelow + "\"/>\n"
                + "        <property name=\"tcs:liftId\" value=\"LIFT_" + tpCode + "\"/>\n"
                + "        <property name=\"tcs:liftFloor\" value=\"" + zz + "\"/>\n"
                + "        <pointLayout labelOffsetX=\"-10\" labelOffsetY=\"-20\" layerId=\"" + (zz - 1) + "\"/>\n"
                + "</point>\n");
            pathValue.append("<path name=\"" + tpName + " --- " + locationCodeAbove + "\" sourcePoint=\"" + tpName + "\" destinationPoint=\"" + locationCodeAbove + "\" length=\"1300\" maxVelocity=\"10000\" maxReverseVelocity=\"0\" locked=\"false\">\n" +
                "        <pathLayout connectionType=\"DIRECT\" layerId=\"" + (zz - 1) + "\"/>\n" +
                "    </path>\n");
            pathValue.append("<path name=\"" + tpName + " --- " + locationCodeBelow + "\" sourcePoint=\"" + tpName + "\" destinationPoint=\"" + locationCodeBelow + "\" length=\"1300\" maxVelocity=\"10000\" maxReverseVelocity=\"0\" locked=\"false\">\n" +
                "        <pathLayout connectionType=\"DIRECT\" layerId=\"" + (zz - 1) + "\"/>\n" +
                "    </path>\n");
          }else if (xx == 8 && (yy == 12 || yy == 15)) {
            String liftCode = locToLift.get(String.format("%02d", xx) + String.format("%02d", yy));
            String liftName = "LIFT_" + "L" + zz  + "_" + liftCode + "_POINT";
            liftBlockMember.computeIfAbsent(liftCode, k -> new ArrayList<>()).add(liftName);
            String locationCodeAbove = locToMap.get(String.format("%02d", xx - 1) + String.format("%02d", yy) + String.format("%02d", zz) + "_P");
            point.append("<point name=\"" + liftName +"\" type=\"HALT_POSITION\" positionX=\"" + positionPointX + "\" positionY=\"" + positionPointY + "\" positionZ=\"0\" vehicleOrientationAngle=\"NaN\">\n"
                + "        <maxVehicleBoundingBox length=\"1000\" width=\"1000\" height=\"1000\" referenceOffsetX=\"0\" referenceOffsetY=\"0\"/>\n"
                + "        <outgoingPath name=\"" + liftName + " --- " + locationCodeAbove + "\"/>\n"
                + "        <property name=\"tcs:liftId\" value=\"LIFT_" + liftCode + "\"/>\n"
                + "        <property name=\"tcs:liftFloor\" value=\"" + zz + "\"/>\n"
                + "        <pointLayout labelOffsetX=\"-10\" labelOffsetY=\"-20\" layerId=\"" + (zz - 1) + "\"/>\n"
                + "</point>\n");
            pathValue.append("<path name=\"" + liftName + " --- " + locationCodeAbove + "\" sourcePoint=\"" + liftName + "\" destinationPoint=\"" + locationCodeAbove + "\" length=\"1300\" maxVelocity=\"10000\" maxReverseVelocity=\"0\" locked=\"false\">\n" +
                "        <pathLayout connectionType=\"DIRECT\" layerId=\"" + (zz - 1) + "\"/>\n" +
                "    </path>\n");
          }
          else {
            point.append("<point name=\"" + locationCode + "_P\" positionX=\"" + positionPointX + "\" positionY=\"" + positionPointY + "\" positionZ=\"0\" vehicleOrientationAngle=\"NaN\" type=\"HALT_POSITION\">\n" +
                "        <maxVehicleBoundingBox length=\"1000\" width=\"1000\" height=\"1000\" referenceOffsetX=\"0\" referenceOffsetY=\"0\"/>\n");
            if (xx > 1) {
              String locationCodeAbove = String.format("%02d", xx - 1) + String.format("%02d", yy) + String.format("%02d", zz) + "_P";
              point.append("        <outgoingPath name=\"" + locationCode + "_P --- " + locationCodeAbove+ "\"/>\n");
              pathValue.append("<path name=\"" + locationCode + "_P --- " + locationCodeAbove + "\" sourcePoint=\"" + locationCode + "_P\" destinationPoint=\"" + locationCodeAbove + "\" length=\"1300\" maxVelocity=\"10000\" maxReverseVelocity=\"0\" locked=\"false\">\n" +
                  "        <pathLayout connectionType=\"DIRECT\" layerId=\"" + (zz - 1) + "\"/>\n" +
                  "    </path>\n");
            }
            if (((xx < x && yy <= 10) || xx < 7)) {
              String locationCodeBelow;
              if ((zz >= 2 && xx + 1 == 7 && yy == 14) || (zz == 2 && xx + 1 == 7 && yy == 13)) {
                String chargeCode = String.format("%02d", ++chargeNum);
                locationCodeBelow = String.format("%02d", xx + 1) + String.format("%02d", yy) + String.format("%02d", zz);
                locToCharge.put(locationCodeBelow, chargeCode);
                locationCodeBelow = "CHARGE_POINT_" + chargeCode;
              }else if (xx == 6 && (yy == 12 || yy == 15)) {
                String loc = String.format("%02d", xx + 2) + String.format("%02d", yy);
                Integer num = liftNumMap.get(loc);
                if (num == null) {
                  num = ++liftNum;
                  liftNumMap.put(loc, num);
                }
                String tpCode = String.format("%02d", num);
                locToLift.put(loc, tpCode);
                locationCodeBelow = "TP_" + "L" + zz  + "_" + tpCode + "_POINT";
                locToMap.put(String.format("%02d", xx + 1) + String.format("%02d", yy) + String.format("%02d", zz) + "_P", locationCodeBelow);
                locToMap.put(String.format("%02d", xx + 2) + String.format("%02d", yy) + String.format("%02d", zz) + "_P", "LIFT_" + "L" + zz  + "_" + tpCode + "_POINT");
              } else{
                locationCodeBelow = String.format("%02d", xx + 1) + String.format("%02d", yy) + String.format("%02d", zz) + "_P";
              }
              point.append("        <outgoingPath name=\"" + locationCode + "_P --- " + locationCodeBelow+ "\"/>\n");
              pathValue.append("<path name=\"" + locationCode + "_P --- " + locationCodeBelow + "\" sourcePoint=\"" + locationCode + "_P\" destinationPoint=\"" + locationCodeBelow + "\" length=\"1300\" maxVelocity=\"10000\" maxReverseVelocity=\"0\" locked=\"false\">\n" +
                  "        <pathLayout connectionType=\"DIRECT\" layerId=\"" + (zz - 1) + "\"/>\n" +
                  "    </path>\n");
            }
            if (yy > 1 && (xx == 2 || xx == 6 || xx == 10 || xx == 15)) {
              String locationCodeLeft = String.format("%02d", xx) + String.format("%02d", yy - 1) + String.format("%02d", zz) + "_P";
              point.append("        <outgoingPath name=\"" + locationCode + "_P --- " + locationCodeLeft+ "\"/>\n");
              pathValue.append("<path name=\"" + locationCode + "_P --- " + locationCodeLeft + "\" sourcePoint=\"" + locationCode + "_P\" destinationPoint=\"" + locationCodeLeft + "\" length=\"1300\" maxVelocity=\"10000\" maxReverseVelocity=\"0\" locked=\"false\">\n" +
                  "        <pathLayout connectionType=\"DIRECT\" layerId=\"" + (zz - 1) + "\"/>\n" +
                  "    </path>\n");
            }
            if ((yy < y && (xx == 2 || xx == 6)) || (yy < 10 && (xx == 10 || xx == 15))) {
              String locationCodeRight = String.format("%02d", xx) + String.format("%02d", yy + 1) + String.format("%02d", zz) + "_P";
              point.append("        <outgoingPath name=\"" + locationCode + "_P --- " + locationCodeRight+ "\"/>\n");
              pathValue.append("<path name=\"" + locationCode + "_P --- " + locationCodeRight + "\" sourcePoint=\"" + locationCode + "_P\" destinationPoint=\"" + locationCodeRight + "\" length=\"1300\" maxVelocity=\"10000\" maxReverseVelocity=\"0\" locked=\"false\">\n" +
                  "        <pathLayout connectionType=\"DIRECT\" layerId=\"" + (zz - 1) + "\"/>\n" +
                  "    </path>\n");
            }

            point.append("    <property name=\"tcs:logicalLayer\" value=\"" + zz + "\"/>\n" +
                "        <pointLayout labelOffsetX=\"-10\" labelOffsetY=\"-20\" layerId=\"" + (zz - 1) + "\"/>\n" +
                "    </point>\n");
          }
          pointValue.append(point);
          String location;
          int positionLocationX = positionPointX - 1500;
          int positionLocationY = positionPointY + 1500;
          if ((zz >= 2 && xx == 7 && yy == 14) || (zz == 2 && xx == 7 && yy == 13)) {
            String chargeCode = locToCharge.get(locationCode);
            location = "<location name=\"CHARGE_STATION_" + chargeCode + "\" positionX=\"" + positionLocationX + "\" positionY=\"" + positionLocationY + "\" positionZ=\"0\" type=\"Recharge\" locked=\"false\">\n"
                + "    <link point=\"CHARGE_POINT_" + chargeCode + "\"/>\n"
                + "    <property name=\"capacity\" value=\"1\"/>\n"
                + "    <property name=\"chargingPower\" value=\"10\"/>\n"
                + "        <locationLayout labelOffsetX=\"-10\" labelOffsetY=\"-20\" locationRepresentation=\"DEFAULT\" layerId=\"" + (zz - 1) + "\"/>\n"
                + "</location>\n";
          } else if (xx == 7 && (yy == 12 || yy == 15)) {
            String tpCode = locToLift.get(String.format("%02d", xx + 1) + String.format("%02d", yy));
            String tpName = "TP_" + "L" + zz  + "_" + tpCode + "_POINT";
            String tpLocationName = "TP_" + "L" + zz  + "_" + tpCode + "_LOCATION";
            location = "<location name=\"" + tpLocationName + "\" positionX=\"" + positionLocationX + "\" positionY=\"" + positionLocationY + "\" positionZ=\"0\" type=\"Transfer Point\" locked=\"false\">\n"
                + "    <link point=\"" + tpName + "\"/>\n"
                + "     <property name=\"tcs:liftId\" value=\"LIFT_" + tpCode + "\"/>\n"
                + "    <property name=\"tcs:targetFloor\" value=\"ALL\"/>\n"
                + "        <locationLayout labelOffsetX=\"-10\" labelOffsetY=\"-20\" locationRepresentation=\"DEFAULT\" layerId=\"" + (zz - 1) + "\"/>\n"
                + "</location>\n";
          } else if (xx == 8 && (yy == 12 || yy == 15)) {
            String liftCode = locToLift.get(String.format("%02d", xx) + String.format("%02d", yy));
            String liftName = "LIFT_" + "L" + zz  + "_" + liftCode + "_POINT";
            String liftLocationName = "LIFT_" + "L" + zz  + "_" + liftCode + "_LOCATION";
            location = "<location name=\"" + liftLocationName + "\" positionX=\"" + positionLocationX + "\" positionY=\"" + positionLocationY + "\" positionZ=\"0\" type=\"Lift\" locked=\"false\">\n"
                + "    <link point=\"" + liftName + "\"/>\n"
                + "     <property name=\"tcs:liftId\" value=\"LIFT_" + liftCode + "\"/>\n"
                + "    <property name=\"tcs:targetFloor\" value=\"ALL\"/>\n"
                + "        <locationLayout labelOffsetX=\"-10\" labelOffsetY=\"-20\" locationRepresentation=\"DEFAULT\" layerId=\"" + (zz - 1) + "\"/>\n"
                + "</location>\n";
          } else{
            if (xx == 2 || xx == 6 || xx == 10 || xx == 15) {
              continue;
            }
            if ((yy == 4 && (xx >= 3 && xx <= 15)) || (yy == 12 && (xx >= 3 && xx <= 6))) {
              continue;
            }
            location = "    <location name=\"" + locationCode + "_L" + "\" positionX=\"" + positionLocationX + "\" positionY=\"" + positionLocationY + "\" positionZ=\"0\" locked=\"false\" type=\"" + locationType + "\">\n" +
                "        <link point=\"" + locationCode + "_P\"/>\n"  +
                "        <property name=\"inventory:status\" value=\"EMPTY\"/>\n" +
                "        <property name=\"inventory:lastUpdate\" value=\"2025-01-01T00:00:00Z\"/>\n" +
                "        <locationLayout labelOffsetX=\"-10\" labelOffsetY=\"-20\" locationRepresentation=\"DEFAULT\" layerId=\"" + (zz - 1) + "\"/>\n" +
                "    </location>\n";
          }
          locationValue.append(location);
        }
      }
    }

    for (String value : locToLift.values()) {
      for (int i = 1; i <= z; i++) {
        for (int j = 1; j <= z; j++) {
          if (i != j) {
            String name = "LIFT_" + value + "_F" + i + "_TO_" + "F" + j;
            liftBlockMember.computeIfAbsent(value, k -> new ArrayList<>()).add(name);
            pathValue.append("<path name=\"" + name + "\" \n"
                + "      sourcePoint=\"" + "LIFT_L" + i + "_" + value + "_POINT\"\n"
                + "      destinationPoint=\"" + "LIFT_L" + j + "_" + value + "_POINT\"\n"
                + "      length=\"1400\" \n"
                + "      maxVelocity=\"1000\" \n"
                + "      maxReverseVelocity=\"0\""
                + "      locked=\"false\">\n"
                + "    <property name=\"tcs:virtualPath\" value=\"true\"/>\n"
                + "    <property name=\"tcs:estimatedTime\" value=\"10\"/>  <!-- 升降10秒 -->\n"
                + "</path>\n");
          }
        }
      }
    }

    StringBuilder tpBlockValue = new StringBuilder();
    for (Map.Entry<String, List<String>> integerListEntry : tpBlockMember.entrySet()) {
      String point = integerListEntry.getValue().get(0).replace("_POINT", "");
      int zAxis = Integer.valueOf(integerListEntry.getKey().substring(4, 6));
      tpBlockValue.append("<block name=\"LIFT_" + point.substring(point.length() - 2) + "_TP_L" +zAxis + "\" type=\"SINGLE_VEHICLE_ONLY\">\n");
      for (String s : integerListEntry.getValue()) {
        tpBlockValue.append("<member name=\"" + s + "\"/>").append("\n");
      }
      tpBlockValue.append("<blockLayout color=\"#FF0000\"/>\n"
          + "</block>\n");
    }

    StringBuilder liftBlockValue = new StringBuilder();
    for (Map.Entry<String, List<String>> integerListEntry : liftBlockMember.entrySet()) {
      liftBlockValue.append("<block name=\"LIFT_" + integerListEntry.getKey() + "_CHAMBER\" type=\"SINGLE_VEHICLE_ONLY\">\n");
      for (String s : integerListEntry.getValue()) {
        liftBlockValue.append("<member name=\"" + s + "\"/>").append("\n");
      }
      liftBlockValue.append("<blockLayout color=\"#FF0000\"/>\n"
          + "</block>\n");
    }


    StringBuilder visualLayoutValue = new StringBuilder("    <visualLayout name=\"VLayout\" scaleX=\"50.0\" scaleY=\"50.0\">\n");
    for (int i = 0; i < z; i++) {
      visualLayoutValue.append("        <layer id=\"" + i + "\" ordinal=\"0\" visible=\"true\" name=\"Layer" + i + "\" groupId=\"0\"/>\n");
    }
    visualLayoutValue.append("<layerGroup id=\"0\" name=\"Default layer group\" visible=\"true\"/>\n"
        + "</visualLayout>\n");
//    System.out.println(head + pointValue + pathValue + vehicleValue + locationTypeValue + locationValue + visualLayoutValue + end);
    System.out.println(head + pointValue + pathValue + vehicleValue + locationTypeValue + locationValue + tpBlockValue + liftBlockValue + visualLayoutValue + end);
  }
}
