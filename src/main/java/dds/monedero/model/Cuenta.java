package dds.monedero.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import dds.monedero.exceptions.MaximaCantidadDepositosException;
import dds.monedero.exceptions.MaximoExtraccionDiarioException;
import dds.monedero.exceptions.MontoNegativoException;
import dds.monedero.exceptions.SaldoMenorException;

public class Cuenta {

  private double saldo = 0;
  private double maximoExtraccionDiaria = 1000;
  private List<Movimiento> movimientos = new ArrayList<>();

  public Cuenta() {
    saldo = 0;
  }

  public Cuenta(double montoInicial) {
    saldo = montoInicial;
  }

  public void setMovimientos(List<Movimiento> movimientos) {
    this.movimientos = movimientos;
  }

  public void poner(double cuanto) {

    montoEsValido(cuanto);

    puedeDepositar();

    new Movimiento(LocalDate.now(), cuanto).agregateA(this);
  }

  public void sacar(double cuanto) {

    montoEsValido(cuanto);

    tieneSaldo(cuanto);

    tieneLimiteDiario(cuanto);

    new Movimiento(LocalDate.now(), cuanto).agregateA(this);
  }

  public void agregarMovimiento(LocalDate fecha, double cuanto, boolean esDeposito) {
    Movimiento movimiento = new Movimiento(fecha, cuanto);
    movimientos.add(movimiento);
  }

  public double getMontoExtraidoA(LocalDate fecha) {
    return getMovimientos().stream()
        .filter(movimiento -> !movimiento.isDeposito() && movimiento.getFecha().equals(fecha))
        .mapToDouble(Movimiento::getMonto)
        .sum();
  }

  public List<Movimiento> getMovimientos() {
    return movimientos;
  }

  public double getSaldo() {
    return saldo;
  }

  public void setSaldo(double saldo) {
    this.saldo = saldo;
  }

  private void montoEsValido(double monto){
    if(monto <= 0){
      throw new MontoNegativoException(monto + ": el monto a ingresar debe ser un valor positivo");
    }
  }

  private boolean puedeDepositar(){
    int depositosDeHoy = (int) getMovimientos().stream().filter(movimiento -> movimiento.isDeposito()).count();

    if (depositosDeHoy >= 3);
    throw new MaximaCantidadDepositosException("Ya excedio los " + 3 + " depositos diarios");
  }

  private double obtenerMontoExtraibleActual(){
    double montoExtraidoHoy = getMontoExtraidoA(LocalDate.now());
    return maximoExtraccionDiaria - montoExtraidoHoy;
  }

  private void tieneSaldo(double monto){
    if(getSaldo() < monto){
      throw new SaldoMenorException("No puede sacar mas de " + getSaldo() + " $");
    }
  }

  private void tieneLimiteDiario(double monto){
    double limite = obtenerMontoExtraibleActual();
    if (monto > limite) {
      throw new MaximoExtraccionDiarioException("No puede extraer mas de $ " + 1000
              + " diarios, límite: " + limite);
    }
  }

}
