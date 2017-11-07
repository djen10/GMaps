package com.example.justin.gmaps.location;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Looper;
import android.text.TextUtils;

import com.example.justin.gmaps.datamodel.data.DataConst;
import com.example.justin.gmaps.datamodel.data.LocationData;
import com.example.justin.gmaps.db.DBUtils;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by Justin on 2017-09-05.
 */

public class LocationHandler implements DataConst {
    ArrayList<LocationData> mLocationDataList = new ArrayList<>();
    private final static String ERROR_STRING = "ERROR";

    // 주소 받아오기
    public static String getAddress(Context context, double lati, double longi) {
        String realAddress = " NULL ";
        Geocoder geocoder = new Geocoder(context, Locale.KOREA);
        List<Address> address;
        try {
            if (geocoder != null) {
                address = geocoder.getFromLocation(lati, longi, 1);
                if (address != null && address.size() > 0) {
                    String currentAddress = address.get(0).getAddressLine(0).toString();
                    realAddress = currentAddress;

                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return realAddress;
    }

    // date형 String 에서 long형 date 로
    public long parseTolong(String str1) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");
        Date t1;
        long result = -1;
        try {
            t1 = simpleDateFormat.parse(str1);
            result = t1.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return result;
    }

    //Long 형 date 를 가져와서 date 포맷으로 변경
    public static String parseToDate(long num) {
        Date date = new Date(num);
        SimpleDateFormat date_format = new SimpleDateFormat("MM/dd HH:mm:ss");
        String formatDate = date_format.format(date);
        return formatDate;
    }

    //Long 형 date 와 포맷을 가져와서 날짜 추출후 포맷 리턴
    public String parseToDate(long num, SimpleDateFormat smpleDateFormati) {
        Date date = new Date(num);
        String formatDate = smpleDateFormati.format(date);
        return formatDate;
    }

    //long형 date 2개를 비교 한다.
    public static String compareLongDate(long num1, long num2) {
        // long형으로 바로 계산 milisecond 를 절상해서 쓰기때문에 1초 이하의 오차가 날 수도 있다.
        long result = (num1 - num2) / 1000; // 초로 변환 시켜야 하므로 1000을 나눠줘야 한다.
        long resultHour = (result / 3600);
        long resultMinute = (result % 3600 / 60);
        long resultSecond = (result % 3600 % 60);
        String resultString = "";
        if (resultMinute < 1.0) {
            resultString = ERROR_STRING;
        } else {
            resultString = String.format("%02d", resultHour) + ":" + String.format("%02d", resultMinute) + ":" + String.format("%02d", resultSecond);
        }

        return resultString;
    }

    // 순서
    // step 1 거리를 체크할 중심점을 잡는다.
    // case 1 : 처음의 데이터가 중심점
    // case 2 : 이후의 중심점에서 100m를 벗어난 데이터가 중심점
    // step 2 중심점을 기준으로 다음 데이터와 비교하여 거리를 측정.
    // step 3 거리가 100 m 이하면 계속 저장, 100 m 이상이면 그동안의 데이터를 비교
    // step 3-1 각각의 좌표에 small area 를 설정 ( 25.0 ) 하여 area 안에 들어와 있는 좌표의 갯수를 체크하여 최적의 거점을 찾음.
    // step 4 중심점을 변경하고 tArrayList 들을 초기화 한 후 step 2 로 감.
    public ArrayList<LocationData> updateMaps(Context context) {
        mLocationDataList.clear();
        // 쿼리문을 받는 커서
        Cursor c = DBUtils.query(context);
        // 결과값을 담는 인텐트
        Intent intent = new Intent();

        //사용해야하는 변수모음 클래스
        //MainActivity.LocationMember localMember = new MainActivity.LocationMember();

        // 중심점을 잡는 변수
        int centerOrder = 1;
        double centerLati = -1;
        double centerLongi = -1;
        long centerDate = -1;
        String centerAddress = "";

        // tmep ArrayList들 범위내에 있는 값들만 비교하기위해 생성
        ArrayList<Long> tTime = new ArrayList<Long>();
        ArrayList<LocationData> tLocationDataList = new ArrayList<>();

        // 단순 비교용 변수.
        double compareLati;
        double compareLongi;

        // 비교를 한 후에 저장될 값들을 담아둔다. 여기서 실제 member값으로 넣어준다.
        double resultLati = -1;
        double resultLongi = -1;
        long resultDate = -1;
        String resultAddress = "";
        long resultStayTime = -1;

        // small area 내에 좌표가 가장 많은 small area 찾기
        int maxCount = -1;

        // DB에 저장 된 값이 아닌 화면에 보여주는 좌표의 갯수
        int realCount = 1;

        // 중심점을 잡아 줘야 할때를 구분해주는 플래그
        boolean centerPointFlag = true;

        int count = 0;
        while (c.moveToNext()) {
            // step 1 중심점 잡기
            if (centerPointFlag) {
                centerOrder = c.getInt(c.getColumnIndex(ORDER));
                centerLati = c.getDouble(c.getColumnIndex(LATI));
                centerLongi = c.getDouble(c.getColumnIndex(LONGI));
                centerDate = c.getLong(c.getColumnIndex(DATE));
                centerAddress = c.getString(c.getColumnIndex(ADDRESS));
                resultLati = centerLati;
                resultLongi = centerLongi;
                resultAddress = centerAddress;
                resultDate = centerDate;
                centerPointFlag = false;
                continue;
            }

            // step 2 범위 내 좌표 가져오기 중심점과 현재 커서가 가르키는 좌표를 비교.
            Location locationA = new Location("");
            Location locationB = new Location("");
            locationA.setLatitude(centerLati);
            locationA.setLongitude(centerLongi);
            locationB.setLatitude(c.getDouble(c.getColumnIndex(LATI)));
            locationB.setLongitude(c.getDouble(c.getColumnIndex(LONGI)));
            double distance = locationA.distanceTo(locationB);
            if (distance < 100.0) {
                LocationData tLocationData = new LocationData();

                tLocationData.latitude = c.getDouble(c.getColumnIndex(LATI));
                tLocationData.longitude = c.getDouble(c.getColumnIndex(LONGI));
                tLocationData.longDate = Long.parseLong(c.getString(c.getColumnIndex(DATE)));
                tLocationData.address = c.getString(c.getColumnIndex(ADDRESS));

                tLocationDataList.add(tLocationData);
            } else {
                //스텝3 좌표 가져온거에 small area 설정하고 count함
                for (int i = 0; i < tLocationDataList.size(); i++) {
                    compareLati = tLocationDataList.get(i).latitude;
                    compareLongi = tLocationDataList.get(i).longitude;
                    int tempCount = 0;
                    for (int j = 0; j < tLocationDataList.size(); j++) {
                        //거리가 25 이상이면 1을 더한다.
                        tempCount += distance(new LatLng(compareLati, compareLongi), new LatLng(tLocationDataList.get(j).latitude, tLocationDataList.get(j).longitude));
                    }
                    // count 로 판별 가능할경우 순서대로 도니까 date까지 안가도 된다. 그러네!
                    if (tempCount > maxCount) {
                        maxCount = tempCount;
                        //그러면 i 번째 좌표가 거점으로 삼기 좋다는 말이다.
                        resultLati = tLocationDataList.get(i).latitude;
                        resultLongi = tLocationDataList.get(i).longitude;
                        resultAddress = tLocationDataList.get(i).address;
                        resultDate = tLocationDataList.get(i).longDate;
                    }

                }
                //for문 다돌았으면 result에는 거점으로 삼아도 되는 곳이 나온다.

                if (mLocationDataList.size() == 0) {
                    LocationData mLocationData = new LocationData();

                    mLocationData.order = realCount++;
                    mLocationData.latitude = centerLati;
                    mLocationData.longitude = centerLongi;
                    mLocationData.date = parseToDate(centerDate);
                    mLocationData.address = centerAddress;

                    mLocationDataList.add(mLocationData);
                    tTime.add(centerDate);

                    tLocationDataList.clear();
                } else {
                    LocationData mLocationData = new LocationData();

                    mLocationData.order = realCount++;
                    mLocationData.latitude = resultLati;
                    mLocationData.longitude = resultLongi;
                    mLocationData.date = parseToDate(resultDate);
                    mLocationData.address = resultAddress;

                    mLocationDataList.add(mLocationData);
                    tTime.add(resultDate);
                    tLocationDataList.clear();
                }

                //새로운 중심점 잡기
                centerOrder = c.getInt(c.getColumnIndex(ORDER));
                centerLati = c.getDouble(c.getColumnIndex(LATI));
                centerLongi = c.getDouble(c.getColumnIndex(LONGI));
                centerDate = c.getLong(c.getColumnIndex(DATE));
                centerAddress = c.getString(c.getColumnIndex(ADDRESS));
                maxCount = -1;
            }
        }

        //마지막 데이터는 무조건 좌표에 뿌려준다. 그래서 커서를 마지막으로 움직이고 추가!
        if (c.moveToLast()) {
            LocationData mLocationData = new LocationData();

            mLocationData.order = realCount++;
            mLocationData.latitude = c.getDouble(c.getColumnIndex(LATI));
            mLocationData.longitude = c.getDouble(c.getColumnIndex(LONGI));
            mLocationData.date = parseToDate(c.getLong(c.getColumnIndex(DATE)));
            mLocationData.address = c.getString(c.getColumnIndex(ADDRESS));
            mLocationDataList.add(mLocationData);

            tTime.add(c.getLong(c.getColumnIndex(DATE)));
        }

        for (int i = 0; i < mLocationDataList.size() - 1; i++) {
            String result = compareLongDate(tTime.get(i + 1), tTime.get(i));
            if (TextUtils.equals(result, ERROR_STRING)) {
                // tTime에 저장되어있는 1분이하의 값 과 그와 매칭되는 값들을 제거한다. 그 이후 i를 뒤로 돌려 에러 이후의 값과 비교를 하게 한다.
                mLocationDataList.remove(i);
                tTime.remove(i);
                i--;
            } else {
                mLocationDataList.get(count++).stayTime = result;
            }
        }
        if(mLocationDataList.size() != 0){
            mLocationDataList.get(mLocationDataList.size() - 1).stayTime = "현재 위치";
        }
        return mLocationDataList;
    }


    // 2개의 위도 경도를 넣으면 거리를 계산한다. 거리가 25이상이면 1을 아니면 0을 리턴한다.
    public static int distance(LatLng latLngA, LatLng latLngB) {
        Location locationA = new Location("");
        Location locationB = new Location("");
        locationA.setLatitude(latLngA.latitude);
        locationA.setLongitude(latLngA.longitude);
        locationB.setLatitude(latLngB.latitude);
        locationB.setLongitude(latLngB.longitude);
        if (locationA.distanceTo(locationB) > 25.0) {
            return 1;
        } else {
            return 0;
        }

    }
}
